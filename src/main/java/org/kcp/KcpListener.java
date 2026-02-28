package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;

public interface KcpListener {
    void onConnected(Ukcp ukcp);
    void handleReceive(ByteBuf byteBuf, Ukcp ukcp);
    void handleException(Throwable ex, Ukcp ukcp);
    void handleClose(Ukcp ukcp);
}