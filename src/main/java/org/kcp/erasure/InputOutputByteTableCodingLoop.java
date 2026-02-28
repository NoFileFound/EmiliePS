package org.kcp.erasure;

public class InputOutputByteTableCodingLoop extends CodingLoopBase {
    @Override
    public void codeSomeShards(byte[][] matrixRows, byte[][] inputs, int inputCount, byte[][] outputs, int outputCount, int offset, int byteCount) {
        final byte[][] table = Galois.MULTIPLICATION_TABLE;
        {
            final int iInput = 0;
            final byte[] inputShard = inputs[iInput];
            for(int i = 0; i < outputCount; i++) {
                final byte[] outputShard = outputs[i];
                final byte[] matrixRow = matrixRows[i];
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for(int j = offset; j < offset + byteCount; j++) {
                    outputShard[j] = multTableRow[inputShard[j] & 0xFF];
                }
            }
        }

        for(int iInput = 1; iInput < inputCount; iInput++) {
            final byte[] inputShard = inputs[iInput];
            for(int i = 0; i < outputCount; i++) {
                final byte[] outputShard = outputs[i];
                final byte[] matrixRow = matrixRows[i];
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for(int j = offset; j < offset + byteCount; j++) {
                    outputShard[j] ^= multTableRow[inputShard[j] & 0xFF];
                }
            }
        }
    }

    @Override
    public boolean checkSomeShards(byte[][] matrixRows, byte[][] inputs, int inputCount, byte[][] toCheck, int checkCount, int offset, int byteCount, byte[] tempBuffer) {
        if(tempBuffer == null) {
            return super.checkSomeShards(matrixRows, inputs, inputCount, toCheck, checkCount, offset, byteCount, null);
        }

        final byte[][] table = Galois.MULTIPLICATION_TABLE;
        for(int i = 0; i < checkCount; i++) {
            final byte[] outputShard = toCheck[i];
            final byte[] matrixRow = matrixRows[i];
            {
                final int iInput = 0;
                final byte[] inputShard = inputs[iInput];
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for(int iByte = offset; iByte < offset + byteCount; iByte++) {
                    tempBuffer[iByte] = multTableRow[inputShard[iByte] & 0xFF];
                }
            }

            for(int iInput = 1; iInput < inputCount; iInput++) {
                final byte[] inputShard = inputs[iInput];
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