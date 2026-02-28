package org.kcp.erasure;

public interface CodingLoop {
     void codeSomeShards(final byte[][] matrixRows, final byte[][] inputs, final int inputCount, final byte[][] outputs, final int outputCount, final int offset, final int byteCount);
     boolean checkSomeShards(final byte[][] matrixRows, final byte[][] inputs, final int inputCount, final byte[][] toCheck, final int checkCount, final int offset, final int byteCount, final byte[] tempBuffer);
}