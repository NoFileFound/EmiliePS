package org.kcp.erasure.bytebuf;

// Imports
import io.netty.buffer.ByteBuf;
import org.kcp.erasure.Galois;

public class InputOutputByteBufHeapTableCodingLoop extends ByteBufCodingLoopBase {
    @Override
    public void codeSomeShards(byte[][] matrixRows, ByteBuf[] inputs, int inputCount, ByteBuf[] outputs, int outputCount, int offset, int byteCount) {
        final byte[][] table = Galois.MULTIPLICATION_TABLE;
        final int count = offset + byteCount;
        final byte[] inputShard = new byte[count];
        final byte[] outputShard = new byte[count];
        final int i = 0;
        inputs[i].getBytes(0,inputShard);
        for(int j = 0; j < outputCount; j++) {
            outputs[j].getBytes(0,outputShard);
            final byte[] matrixRow = matrixRows[j];
            final byte[] multTableRow = table[matrixRow[i] & 0xFF];
            for(int k = offset; k < count; k++) {
                outputShard[k] = multTableRow[inputShard[k] & 0xFF];
            }

            outputs[j].setBytes(0,outputShard);
        }

        for(int j = 1; j < inputCount; j++) {
            inputs[j].getBytes(0,inputShard);
            for(int k = 0; k < outputCount; k++) {
                outputs[k].getBytes(0,outputShard);
                final byte[] matrixRow = matrixRows[k];
                final byte[] multTableRow = table[matrixRow[j] & 0xFF];
                for(int l = offset; l < count; l++) {
                    outputShard[l] ^= multTableRow[inputShard[l] & 0xFF];
                }

                outputs[k].setBytes(0,outputShard);
            }
        }
    }

    @Override
    public boolean checkSomeShards(byte[][] matrixRows, ByteBuf[] inputs, int inputCount, byte[][] toCheck, int checkCount, int offset, int byteCount, byte[] tempBuffer) {
        if(tempBuffer == null) {
            return super.checkSomeShards(matrixRows, inputs, inputCount, toCheck, checkCount, offset, byteCount, null);
        }

        final byte[] inputShard = new byte[offset + byteCount];
        final byte[][] table = Galois.MULTIPLICATION_TABLE;
        for(int i = 0; i < checkCount; i++) {
            final byte[] outputShard = toCheck[i];
            final byte[] matrixRow = matrixRows[i];
            {
                final int j = 0;
                inputs[j].getBytes(0,inputShard);
                final byte[] multTableRow = table[matrixRow[j] & 0xFF];
                for(int iByte = offset; iByte < offset + byteCount; iByte++) {
                    tempBuffer[iByte] = multTableRow[inputShard[iByte] & 0xFF];
                }
            }

            for(int iInput = 1; iInput < inputCount; iInput++) {
                inputs[iInput].getBytes(0,inputShard);
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for(int iByte = offset; iByte < offset + byteCount; iByte++) {
                    tempBuffer[iByte] ^= multTableRow[inputShard[iByte] & 0xFF];
                }
            }

            for(int iByte = offset; iByte < offset + byteCount; iByte++) {
                if(tempBuffer[iByte] != outputShard[iByte]) {
                    return false;
                }
            }
        }

        return true;
    }
}