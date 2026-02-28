package org.kcp.erasure;

// Imports
import io.netty.buffer.ByteBuf;

public interface IFecEncode {
    ByteBuf[] encode(final ByteBuf byteBuf);
    void release();
}