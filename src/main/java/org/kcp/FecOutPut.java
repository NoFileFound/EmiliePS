package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import org.kcp.erasure.IFecEncode;

public class FecOutPut implements  KcpOutput{
    private KcpOutput output;
    private IFecEncode fecEncode;

    protected FecOutPut(KcpOutput output, IFecEncode fecEncode) {
        this.output = output;
        this.fecEncode = fecEncode;
    }

    @Override
    public void out(ByteBuf msg, IKcp kcp) {
        ByteBuf[] byteBufs = fecEncode.encode(msg);
        output.out(msg,kcp);
        if(byteBufs==null) {
            return;
        }
        for (int i = 0; i < byteBufs.length; i++) {
            ByteBuf parityByteBuf = byteBufs[i];
            output.out(parityByteBuf,kcp);
        }
    }
}