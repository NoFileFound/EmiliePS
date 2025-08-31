package org.kcp.erasure;

// Imports
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.kcp.erasure.fec.FecDecode;
import org.kcp.erasure.fec.FecEncode;

public class FecAdapt {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(FecAdapt.class);
    private final ReedSolomon reedSolomon;

    public FecAdapt(int dataShards, int parityShards){
        reedSolomon = ReedSolomon.create(dataShards,parityShards);
        log.info("fec use jvm reedSolomon dataShards {} parityShards {}",dataShards,parityShards);
    }

    public IFecEncode fecEncode(int headerOffset,int mtu){
        return new FecEncode(headerOffset,this.reedSolomon,mtu);
    }

    public IFecDecode fecDecode(int mtu){
        return new FecDecode(3* this.reedSolomon.getTotalShardCount(), this.reedSolomon,mtu);
    }
}