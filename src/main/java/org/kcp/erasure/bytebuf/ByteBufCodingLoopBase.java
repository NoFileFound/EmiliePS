package org.kcp.erasure.bytebuf;

// Imports
import io.netty.buffer.ByteBuf;
import org.kcp.erasure.Galois;

public abstract class ByteBufCodingLoopBase implements ByteBufCodingLoop {
    @Override
    public boolean checkSomeShards(byte[][] matrixRows, ByteBuf[] inputs, int inputCount, byte[][] toCheck, int checkCount, int offset, int byteCount, byte[] tempBuffer) {
        byte [][] table = Galois.MULTIPLICATION_TABLE;
        for(int i = offset; i < offset + byteCount; i++) {
            for(int j = 0; j < checkCount; j++) {
                byte [] matrixRow = matrixRows[j];
                int value = 0;
                for(int k = 0; k < inputCount; k++) {
                    value ^= table[matrixRow[k] & 0xFF][inputs[k].getByte(i) & 0xFF];
                }

                if(toCheck[j][i] != (byte)value) {
                    return false;
                }
            }
        }

        return true;
    }
}