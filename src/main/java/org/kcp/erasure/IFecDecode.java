package org.kcp.erasure;

// Imports
import java.util.List;
import io.netty.buffer.ByteBuf;
import org.kcp.erasure.fec.FecPacket;

public interface IFecDecode {
    List<ByteBuf> decode(final FecPacket pkt);
    void release();
}