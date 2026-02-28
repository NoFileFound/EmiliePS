package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import org.kcp.erasure.IFecEncode;

public class FecOutPut implements  KcpOutput{
    private final KcpOutput output;
    private final IFecEncode fecEncode;

    protected FecOutPut(KcpOutput output, IFecEncode fecEncode) {
        this.output = output;
        this.fecEncode = fecEncode;
    }

    @Override
    public void out(ByteBuf msg, IKcp kcp) {
        ByteBuf[] byteBufs = fecEncode.encode(msg);
        output.out(msg,kcp);
        if(byteBufs == null) {
            return;
        }

        for(ByteBuf parityByteBuf : byteBufs) {
            output.out(parityByteBuf, kcp);
        }
    }
}