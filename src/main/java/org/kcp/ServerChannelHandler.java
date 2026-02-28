package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.HashedWheelTimer;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.kcp.erasure.fec.Fec;
import org.kcp.threadPool.IMessageExecutor;
import org.kcp.threadPool.IMessageExecutorPool;
import org.kcp.threadPool.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    record HandshakeWaiter(long convId, InetSocketAddress address) {}
    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);
    private final ServerConvChannelManager channelManager;
    private final ChannelConfig channelConfig;
    private final IMessageExecutorPool iMessageExecutorPool;
    private final KcpListener kcpListener;
    private final HashedWheelTimer hashedWheelTimer;
    private final ConcurrentLinkedQueue<HandshakeWaiter> handshakeWaiters = new ConcurrentLinkedQueue<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public ServerChannelHandler(IChannelManager channelManager, ChannelConfig channelConfig, IMessageExecutorPool iMessageExecutorPool, KcpListener kcpListener, HashedWheelTimer hashedWheelTimer) {
        this.channelManager = (ServerConvChannelManager) channelManager;
        this.channelConfig = channelConfig;
        this.iMessageExecutorPool = iMessageExecutorPool;
        this.kcpListener = kcpListener;
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("", cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        final ChannelConfig channelConfig = this.channelConfig;
        DatagramPacket msg = (DatagramPacket) object;
        ByteBuf byteBuf = msg.content();
        User user = new User(ctx.channel(), msg.sender(), msg.recipient());
        Ukcp ukcp = channelManager.get(msg);
        if(byteBuf.readableBytes() == 20) {
            HandshakeWaiter waiter = handshakeWaitersFind(user.getRemoteAddress());
            long convId;
            if(waiter == null) {
                synchronized(channelManager) {
                    do {
                        convId = secureRandom.nextLong();
                    } while(channelManager.convExists(convId) || handshakeWaitersFind(convId) != null);
                }
                handshakeWaitersAppend(new HandshakeWaiter(convId, user.getRemoteAddress()));
            } else {
                convId = waiter.convId;
            }

            handleEnet(byteBuf, ukcp, user, convId);
            msg.release();
            return;
        }

        boolean newConnection = false;
        IMessageExecutor iMessageExecutor = iMessageExecutorPool.getIMessageExecutor();
        if(ukcp == null) {
            HandshakeWaiter waiter = handshakeWaitersFind(byteBuf.getLong(0));
            if(waiter == null) {
                msg.release();
                return;
            } else {
                handshakeWaitersRemove(waiter);
                int sn = getSn(byteBuf, channelConfig);
                if(sn != 0) {
                    msg.release();
                    return;
                }

                KcpOutput kcpOutput = new KcpOutPutImp();
                Ukcp newUkcp = new Ukcp(kcpOutput, kcpListener, iMessageExecutor, channelConfig, channelManager);
                newUkcp.user(user);
                newUkcp.setConv(waiter.convId);
                channelManager.New(msg.sender(), newUkcp, msg);
                hashedWheelTimer.newTimeout(new ScheduleTask(iMessageExecutor, newUkcp, hashedWheelTimer), newUkcp.getInterval(), TimeUnit.MILLISECONDS);
                ukcp = newUkcp;
                newConnection = true;
            }
        }

        iMessageExecutor.execute(new UkcpEventSender(newConnection, ukcp, byteBuf, msg.sender()));
    }

    public void handshakeWaitersAppend(HandshakeWaiter handshakeWaiter){
        if(handshakeWaiters.size() > 10) {
            handshakeWaiters.poll();
        }

        handshakeWaiters.add(handshakeWaiter);
    }

    public void handshakeWaitersRemove(HandshakeWaiter handshakeWaiter){
        handshakeWaiters.remove(handshakeWaiter);
    }

    public HandshakeWaiter handshakeWaitersFind(long conv) {
        for(HandshakeWaiter waiter : handshakeWaiters) {
            if(waiter.convId == conv) {
                return waiter;
            }
        }

        return null;
    }

    public HandshakeWaiter handshakeWaitersFind(InetSocketAddress address) {
        for(HandshakeWaiter waiter : handshakeWaiters) {
            if(waiter.address.equals(address)) {
                return waiter;
            }
        }

        return null;
    }

    public static void handleEnet(ByteBuf data, Ukcp ukcp, User user, long conv) {
        if(data == null || data.readableBytes() != 20) {
            return;
        }

        int code = data.readInt();
        data.readUnsignedIntLE();
        data.readUnsignedIntLE();
        int enet = data.readInt();
        data.readUnsignedInt();
        try {
            switch (code) {
                case 255 -> { // Connect + Handshake
                    if(user!=null) {
                        Ukcp.sendHandshakeRsp(user, enet, conv);
                    }
                }
                case 404 -> { // Disconnect
                    if(ukcp!=null) {
                        ukcp.close(false);
                    }
                }
            }
        } catch(Throwable ignore){}
    }

    static class UkcpEventSender implements ITask {
        private final boolean newConnection;
        private final Ukcp ukcp;
        private final ByteBuf byteBuf;
        private final InetSocketAddress sender;

        UkcpEventSender(boolean newConnection, Ukcp ukcp, ByteBuf byteBuf, InetSocketAddress sender) {
            this.newConnection = newConnection;
            this.ukcp = ukcp;
            this.byteBuf = byteBuf;
            this.sender = sender;
        }

        @Override
        public void execute() {
            if(newConnection) {
                try {
                    ukcp.getKcpListener().onConnected(ukcp);
                } catch (Throwable throwable) {
                    ukcp.getKcpListener().handleException(throwable, ukcp);
                }
            }

            ukcp.user().setRemoteAddress(sender);
            ukcp.read(byteBuf);
        }
    }

    private int getSn(ByteBuf byteBuf,ChannelConfig channelConfig){
        int headerSize = 0;
        if(channelConfig.getFecAdapt() != null) {
            headerSize+= Fec.fecHeaderSizePlus2;
        }

        return byteBuf.getIntLE(byteBuf.readerIndex() + Kcp.IKCP_SN_OFFSET + headerSize);
    }
}