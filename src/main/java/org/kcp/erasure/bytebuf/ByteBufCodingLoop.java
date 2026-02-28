package org.kcp.erasure.bytebuf;

// Imports
import io.netty.buffer.ByteBuf;

public interface ByteBufCodingLoop {
    void codeSomeShards(final byte[][] matrixRows, final ByteBuf[] inputs, final int inputCount, final ByteBuf[] outputs, final int outputCount, final int offset, final int byteCount);
    boolean checkSomeShards(final byte[][] matrixRows, final ByteBuf[] inputs, final int inputCount, final byte[][] toCheck, final int checkCount, final int offset, final int byteCount, final byte[] tempBuffer);
}