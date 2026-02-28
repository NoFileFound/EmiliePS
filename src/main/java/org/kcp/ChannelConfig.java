package org.kcp;

// Imports
import lombok.Getter;
import lombok.Setter;
import org.kcp.erasure.FecAdapt;
import org.kcp.threadPool.IMessageExecutorPool;
import org.kcp.threadPool.netty.NettyMessageExecutorPool;

public class ChannelConfig {
    private IMessageExecutorPool iMessageExecutorPool = new NettyMessageExecutorPool(Runtime.getRuntime().availableProcessors());
    public static final int crc32Size = 4;
    @Getter @Setter private long conv;
    @Getter private boolean nodelay;
    @Getter private int interval = Kcp.IKCP_INTERVAL;
    @Getter private int fastresend;
    @Getter private boolean nocwnd;
    @Getter @Setter private int sndwnd = Kcp.IKCP_WND_SND;
    @Getter @Setter private int rcvwnd = Kcp.IKCP_WND_RCV;
    @Getter @Setter private int mtu = Kcp.IKCP_MTU_DEF;
    @Getter @Setter private long timeoutMillis;
    @Getter @Setter private boolean stream;
    @Getter @Setter private FecAdapt fecAdapt;
    @Getter @Setter private boolean ackNoDelay = false;
    @Getter @Setter private boolean fastFlush = true;
    @Getter @Setter private boolean crc32Check = false;
    @Setter @Getter private int readBufferSize = -1;
    @Getter @Setter private int writeBufferSize = -1;
    @Getter @Setter private int ackMaskSize = 0;
    @Getter @Setter private boolean useConvChannel = false;

    public void nodelay(boolean nodelay, int interval, int resend, boolean nc) {
        this.nodelay = nodelay;
        this.interval = interval;
        this.fastresend = resend;
        this.nocwnd = nc;
    }

    public IMessageExecutorPool getiMessageExecutorPool() {
        return iMessageExecutorPool;
    }

    public void setiMessageExecutorPool(IMessageExecutorPool iMessageExecutorPool) {
        if(this.iMessageExecutorPool != null) {
            this.iMessageExecutorPool.stop();
        }

        this.iMessageExecutorPool = iMessageExecutorPool;
    }
}