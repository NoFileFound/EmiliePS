package org.kcp;

// Imports
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.jctools.queues.MpscLinkedQueue;
import org.kcp.erasure.FecAdapt;
import org.kcp.erasure.IFecDecode;
import org.kcp.erasure.IFecEncode;
import org.kcp.erasure.fec.Fec;
import org.kcp.erasure.fec.FecPacket;
import org.kcp.erasure.fec.Snmp;
import org.kcp.threadPool.IMessageExecutor;

public class Ukcp {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Ukcp.class);
    private final IKcp kcp;
    private boolean fastFlush = true;
    private long tsUpdate = -1;
    @Getter
    private boolean active;
    private IFecEncode fecEncode = null;
    private IFecDecode fecDecode = null;
    private final Queue<ByteBuf> writeBuffer;
    private final Queue<ByteBuf> readBuffer;
    private final IMessageExecutor iMessageExecutor;
    private final KcpListener kcpListener;
    private final long timeoutMillis;
    private final IChannelManager channelManager;
    private final AtomicBoolean writeProcessing = new AtomicBoolean(false);
    private final AtomicBoolean readProcessing = new AtomicBoolean(false);
    private final AtomicInteger readBufferIncr = new AtomicInteger(-1);
    private final AtomicInteger writeBufferIncr = new AtomicInteger(-1);
    private final WriteTask writeTask = new WriteTask(this);
    private final ReadTask readTask = new ReadTask(this);
    private boolean controlReadBufferSize=false;
    private boolean controlWriteBufferSize=false;
    private long lastRecieveTime = System.currentTimeMillis();

    public Ukcp(KcpOutput output, KcpListener kcpListener, IMessageExecutor iMessageExecutor,  ChannelConfig channelConfig, IChannelManager channelManager) {
        this.timeoutMillis = channelConfig.getTimeoutMillis();
        this.kcp = new Kcp(channelConfig.getConv(), output);
        this.active = true;
        this.kcpListener = kcpListener;
        this.iMessageExecutor = iMessageExecutor;
        this.channelManager = channelManager;
        this.writeBuffer = new MpscLinkedQueue<>();
        this.readBuffer = new MpscLinkedQueue<>();
        if(channelConfig.getReadBufferSize() != -1) {
            this.controlReadBufferSize = true;
            this.readBufferIncr.set(channelConfig.getReadBufferSize()/channelConfig.getMtu());
        }

        if(channelConfig.getWriteBufferSize() != -1) {
            this.controlWriteBufferSize = true;
            this.writeBufferIncr.set(channelConfig.getWriteBufferSize()/channelConfig.getMtu());
        }

        int headerSize = 0;
        FecAdapt fecAdapt = channelConfig.getFecAdapt();
        if(channelConfig.isCrc32Check()) {
            headerSize += ChannelConfig.crc32Size;
        }

        if(fecAdapt != null) {
            KcpOutput kcpOutput = kcp.getOutput();
            fecEncode = fecAdapt.fecEncode(headerSize,channelConfig.getMtu());
            fecDecode = fecAdapt.fecDecode(channelConfig.getMtu());
            kcpOutput = new FecOutPut(kcpOutput, fecEncode);
            kcp.setOutput(kcpOutput);
            headerSize+= Fec.fecHeaderSizePlus2;
        }

        kcp.setReserved(headerSize);
        initKcpConfig(channelConfig);
    }

    private void initKcpConfig(ChannelConfig channelConfig) {
        kcp.nodelay(channelConfig.isNodelay(),channelConfig.getInterval(),channelConfig.getFastresend(),channelConfig.isNocwnd());
        kcp.setSndWnd(channelConfig.getSndwnd());
        kcp.setRcvWnd(channelConfig.getRcvwnd());
        kcp.setMtu(channelConfig.getMtu());
        kcp.setStream(channelConfig.isStream());
        kcp.setAckNoDelay(channelConfig.isAckNoDelay());
        kcp.setAckMaskSize(channelConfig.getAckMaskSize());
        this.fastFlush = channelConfig.isFastFlush();
    }

    public static void sendHandshakeReq(User user) {
        ByteBuf packet = Unpooled.buffer(20);
        packet.writeInt(255);
        packet.writeIntLE(0);
        packet.writeIntLE(0);
        packet.writeInt(1234567890);
        packet.writeInt(0xffffffff);
        UDPSend(packet, user);
    }

    public static void sendHandshakeRsp(User user, int enet, long conv) {
        ByteBuf packet = Unpooled.buffer(20);
        packet.writeInt(325);
        packet.writeIntLE((int) (conv >> 32));
        packet.writeIntLE((int) (conv & 0xFFFFFFFFL));
        packet.writeInt(enet);
        packet.writeInt(340870469);
        Ukcp.UDPSend(packet,user);
    }

    public void sendDisconnectPacket(int code) {
        long conv = getConv();
        ByteBuf packet = Unpooled.buffer(20);
        packet.writeInt(404);
        packet.writeIntLE((int) (conv >> 32));
        packet.writeIntLE((int) (conv & 0xFFFFFFFFL));
        packet.writeInt(code);
        packet.writeInt(423728276); // constant?
        UDPSend(packet);
    }

    public void UDPSend(ByteBuf packet) {
        User user = kcp.getUser();
        UDPSend(packet,user);
    }

    public static void UDPSend(ByteBuf packet,User user) {
        DatagramPacket datagramPacket = new DatagramPacket(packet,user.getRemoteAddress(), user.getLocalAddress());
        user.getChannel().writeAndFlush(datagramPacket);
    }

    protected void receive(List<ByteBuf> bufList) {
        kcp.recv(bufList);
    }

    protected ByteBuf mergeReceive() {
        return kcp.mergeRecv();
    }

    protected void input(ByteBuf data,long current) throws IOException {
        Snmp.snmp.InPkts.increment();
        Snmp.snmp.InBytes.add(data.readableBytes());
        if(fecDecode != null) {
            FecPacket fecPacket = FecPacket.newFecPacket(data);
            if(fecPacket.getFlag() == Fec.typeData) {
                data.skipBytes(2);
                input(data, true,current);
            }

            if(fecPacket.getFlag() == Fec.typeData || fecPacket.getFlag() == Fec.typeParity) {
                List<ByteBuf> byteBufs = fecDecode.decode(fecPacket);
                if(byteBufs != null) {
                    ByteBuf byteBuf;
                    for(int i = 0; i < byteBufs.size(); i++) {
                        byteBuf = byteBufs.get(i);
                        input(byteBuf, false,current);
                        byteBuf.release();
                    }
                }
            }
        } else {
            input(data, true,current);
        }
    }

    private void input(ByteBuf data, boolean regular,long current) throws IOException {
        int ret = kcp.input(data, regular,current);
        switch (ret) {
            case -1:
                throw new IOException("No enough bytes of head");
            case -2:
                throw new IOException("No enough bytes of data");
            case -3:
                throw new IOException("Mismatch cmd");
            case -4:
                throw new IOException("Conv inconsistency");
            default:
                break;
        }
    }

    void send(ByteBuf buf) throws IOException {
        int ret = kcp.send(buf);
        if(ret == -2) {
            throw new IOException("Too many fragments");
        }
    }

    protected boolean canRecv() {
        return kcp.canRecv();
    }

    protected long getLastRecieveTime() {
        return lastRecieveTime;
    }

    protected void setLastRecieveTime(long lastRecieveTime) {
        this.lastRecieveTime = lastRecieveTime;
    }

    protected boolean canSend(boolean curCanSend) {
        int max = kcp.getSndWnd() * 2;
        int waitSnd = kcp.waitSnd();
        if(curCanSend) {
            return waitSnd < max;
        } else {
            int threshold = Math.max(1, max / 2);
            return waitSnd < threshold;
        }
    }

    protected long update(long current) {
        kcp.update(current);
        long nextTsUp = check(current);
        setTsUpdate(nextTsUp);
        return nextTsUp;
    }

    protected long flush(long current){
        return kcp.flush(false,current);
    }

    protected long check(long current) {
        return kcp.check(current);
    }

    protected boolean checkFlush() {
        return kcp.checkFlush();
    }

    public long getConv() {
        return kcp.getConv();
    }

    public void setConv(long conv) {
        kcp.setConv(conv);
    }

    protected int getInterval() {
        return kcp.getInterval();
    }

    protected boolean isStream() {
        return kcp.isStream();
    }

    public Ukcp setByteBufAllocator(ByteBufAllocator allocator) {
        kcp.setByteBufAllocator(allocator);
        return this;
    }

    protected boolean isFastFlush() {
        return fastFlush;
    }

    protected void read(ByteBuf byteBuf) {
        if(controlReadBufferSize) {
            int readBufferSize = readBufferIncr.getAndUpdate(operand -> {
                if(operand == 0){
                    return operand;
                }
                return --operand;
            });

            if(readBufferSize == 0){
                ByteBuf pack = this.readBuffer.poll();
                if(pack!=null) {
                    pack.release();
                }
                return;
            }
        }

        this.readBuffer.offer(byteBuf);
        notifyReadEvent();
    }

    public boolean write(ByteBuf byteBuf) {
        if(controlWriteBufferSize) {
            int bufferSize =writeBufferIncr.getAndUpdate(operand -> {
                if(operand == 0) {
                    return operand;
                }

                return --operand;
            });

            if(bufferSize==0){
                return false;
            }
        }

        byteBuf = byteBuf.retainedDuplicate();
        writeBuffer.offer(byteBuf);
        notifyWriteEvent();
        return true;
    }

    protected AtomicInteger getReadBufferIncr() {
        return readBufferIncr;
    }

    public void close() {
        close(true);
    }

    public void close(boolean sendDisconnectPack) {
        if(sendDisconnectPack) {
            sendDisconnectPacket(0);
        }

        this.iMessageExecutor.execute(() -> internalClose());
    }

    private void notifyReadEvent() {
        if(readProcessing.compareAndSet(false,true)) {
            this.iMessageExecutor.execute(this.readTask);
        }
    }

    protected void notifyWriteEvent() {
        if(writeProcessing.compareAndSet(false,true)) {
            this.iMessageExecutor.execute(this.writeTask);
        }
    }

    protected long getTsUpdate() {
        return tsUpdate;
    }

    protected Queue<ByteBuf> getReadBuffer() {
        return readBuffer;
    }

    protected Ukcp setTsUpdate(long tsUpdate) {
        this.tsUpdate = tsUpdate;
        return this;
    }

    protected Queue<ByteBuf> getWriteBuffer() {
        return writeBuffer;
    }

    protected KcpListener getKcpListener() {
        return kcpListener;
    }

    void internalClose() {
        if(!active) {
            return;
        }

        this.active = false;
        notifyReadEvent();
        kcpListener.handleClose(this);
        notifyWriteEvent();
        kcp.flush(false, System.currentTimeMillis());
        channelManager.del(this);
        release();
    }

    void release() {
        kcp.setState(-1);
        kcp.release();
        for(; ;) {
            ByteBuf byteBuf = writeBuffer.poll();
            if(byteBuf == null) {
                break;
            }
            byteBuf.release();
        }

        for(; ;) {
            ByteBuf byteBuf = readBuffer.poll();
            if(byteBuf == null) {
                break;
            }
            byteBuf.release();
        }

        if(this.fecEncode != null) {
            this.fecEncode.release();
        }

        if(this.fecDecode != null) {
            this.fecDecode.release();
        }
    }

    protected AtomicBoolean getWriteProcessing() {
        return writeProcessing;
    }

    protected AtomicBoolean getReadProcessing() {
        return readProcessing;
    }

    protected IMessageExecutor getiMessageExecutor() {
        return iMessageExecutor;
    }

    protected long getTimeoutMillis() {
        return timeoutMillis;
    }

    protected AtomicInteger getWriteBufferIncr() {
        return writeBufferIncr;
    }

    protected boolean isControlReadBufferSize() {
        return controlReadBufferSize;
    }


    protected boolean isControlWriteBufferSize() {
        return controlWriteBufferSize;
    }


    @SuppressWarnings("unchecked")
    public User user() {
        return (User) kcp.getUser();
    }

    public int srtt() {
        return kcp.getSrtt();
    }

    protected Ukcp user(User user) {
        kcp.setUser(user);
        return this;
    }

    @Override
    public String toString() {
        return "Ukcp(" + "getConv=" + kcp.getConv() + ", state=" + kcp.getState() + ", active=" + active + ')';
    }
}