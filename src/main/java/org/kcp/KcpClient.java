package org.kcp;

// Imports
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kcp.erasure.fec.Fec;
import org.kcp.threadPool.IMessageExecutor;
import org.kcp.threadPool.IMessageExecutorPool;

public class KcpClient {
    private IMessageExecutorPool iMessageExecutorPool;
    private Bootstrap bootstrap;
    private EventLoopGroup nioEventLoopGroup;
    @Getter private IChannelManager channelManager;
    private HashedWheelTimer hashedWheelTimer;
    private static class TimerThreadFactory implements ThreadFactory {
        private final AtomicInteger timeThreadName = new AtomicInteger(0);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "KcpClientTimerThread " + timeThreadName.addAndGet(1));
        }
    }

    public void init(ChannelConfig channelConfig, KcpListener kcpListener) {
        if(channelConfig.isUseConvChannel()) {
            int convIndex = 0;
            if(channelConfig.getFecAdapt() != null) {
                convIndex += Fec.fecHeaderSizePlus2;
            }

            channelManager = new ClientConvChannelManager(convIndex);
        } else {
            channelManager = new ClientAddressChannelManager();
        }

        this.iMessageExecutorPool = channelConfig.getiMessageExecutorPool();
        nioEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(), 1, TimeUnit.MILLISECONDS);
        bootstrap = new Bootstrap();
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.group(nioEventLoopGroup);
        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) {
                ClientChannelHandler clientChannelHandler = new ClientChannelHandler(channelManager, channelConfig, iMessageExecutorPool, hashedWheelTimer, kcpListener);
                ChannelPipeline cp = ch.pipeline();
                if(channelConfig.isCrc32Check()) {
                    Crc32Encode crc32Encode = new Crc32Encode();
                    Crc32Decode crc32Decode = new Crc32Decode();
                    cp.addLast(crc32Encode);
                    cp.addLast(crc32Decode);
                }

                cp.addLast(clientChannelHandler);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void reconnect(Ukcp ukcp) {
        if(!(channelManager instanceof ClientConvChannelManager)) {
            throw new UnsupportedOperationException("reconnect can only be used in convChannel");
        }

        ukcp.getiMessageExecutor().execute(() -> {
            User user = ukcp.user();
            user.getChannel().close();
            InetSocketAddress localAddress = new InetSocketAddress(0);
            ChannelFuture channelFuture = bootstrap.connect(user.getRemoteAddress(), localAddress);
            user.setChannel(channelFuture.channel());
        });
    }

    public Ukcp connect(InetSocketAddress localAddress, InetSocketAddress remoteAddress, ChannelConfig channelConfig) {
        if(localAddress == null) {
            localAddress = new InetSocketAddress(0);
        }

        ChannelFuture channelFuture = bootstrap.connect(remoteAddress, localAddress);
        ChannelFuture sync = channelFuture.syncUninterruptibly();
        NioDatagramChannel channel = (NioDatagramChannel) sync.channel();
        localAddress = channel.localAddress();
        User user = new User(channel, remoteAddress, localAddress);
        IMessageExecutor iMessageExecutor = iMessageExecutorPool.getIMessageExecutor();
        KcpOutput kcpOutput = new KcpOutPutImp();
        Ukcp ukcp = new Ukcp(kcpOutput, null, iMessageExecutor, channelConfig, channelManager);
        ukcp.user(user);
        Ukcp.sendHandshakeReq(user);
        return ukcp;
    }

    public Ukcp connect(InetSocketAddress remoteAddress, ChannelConfig channelConfig) {
        return connect(null, remoteAddress, channelConfig);
    }

    public void stop() {
        channelManager.getAll().forEach(ukcp -> {
            try {
                ukcp.close();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        if(iMessageExecutorPool != null) {
            iMessageExecutorPool.stop();
        }

        if(nioEventLoopGroup != null) {
            nioEventLoopGroup.shutdownGracefully();
        }

        if(hashedWheelTimer != null) {
            hashedWheelTimer.stop();
        }
    }
}