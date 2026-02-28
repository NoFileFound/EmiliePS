package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;

public interface KcpOutput {
    void out(ByteBuf data, IKcp kcp);
}