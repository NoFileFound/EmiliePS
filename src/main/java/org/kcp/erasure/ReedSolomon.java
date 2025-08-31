package org.kcp.erasure;

// Imports
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.kcp.erasure.bytebuf.ByteBufCodingLoop;
import org.kcp.erasure.bytebuf.InputOutputByteBufHeapTableCodingLoop;

/**
 * Reed-Solomon Coding over 8-bit values.
 */
public class ReedSolomon {
    @Getter private final int dataShardCount;
    @Getter private final int parityShardCount;
    @Getter private final int totalShardCount;
    private final Matrix matrix;
    private final CodingLoop codingLoop;

    /**
     * Rows from the matrix for encoding parity, each one as its own
     * byte array to allow for efficient access while encoding.
     */
    private final byte [] [] parityRows;

    /**
     * Creates a ReedSolomon codec with the default coding loop.
     */
    public static ReedSolomon create(int dataShardCount, int parityShardCount) {
        return new ReedSolomon(dataShardCount, parityShardCount, new InputOutputByteTableCodingLoop());
    }

    /**
     * Initializes a new encoder/decoder, with a chosen coding loop.
     */
    public ReedSolomon(int dataShardCount, int parityShardCount, CodingLoop codingLoop) {
        if (256 < dataShardCount + parityShardCount) {
            throw new IllegalArgumentException("too many shards - max is 256");
        }

        this.dataShardCount = dataShardCount;
        this.parityShardCount = parityShardCount;
        this.codingLoop = codingLoop;
        this.totalShardCount = dataShardCount + parityShardCount;
        matrix = buildMatrix(dataShardCount, this.totalShardCount);
        parityRows = new byte [parityShardCount] [];
        for (int i = 0; i < parityShardCount; i++) {
            parityRows[i] = matrix.getRow(dataShardCount + i);
        }
    }

    /**
     * Encodes parity for a set of data shards.
     *
     * @param shards An array containing data shards followed by parity shards.
     *               Each shard is a byte array, and they must all be the same
     *               size.
     * @param offset The index of the first byte in each shard to encode.
     * @param byteCount The number of bytes to encode in each shard.
     *
     */
    public void encodeParity(byte[][] shards, int offset, int byteCount) {
        checkBuffersAndSizes(shards, offset, byteCount);
        byte [] [] outputs = new byte [parityShardCount] [];
        System.arraycopy(shards, dataShardCount, outputs, 0, parityShardCount);
        codingLoop.codeSomeShards(parityRows, shards, dataShardCount, outputs, parityShardCount, offset, byteCount);
    }

    private static final ByteBufCodingLoop LOOP = new InputOutputByteBufHeapTableCodingLoop();

    public void encodeParity(ByteBuf[] shards, int offset, int byteCount) {
        checkBuffersAndSizes(shards, offset, byteCount);
        ByteBuf [] outputs = new ByteBuf [parityShardCount] ;
        System.arraycopy(shards, dataShardCount, outputs, 0, parityShardCount);
        LOOP.codeSomeShards(parityRows, shards, dataShardCount, outputs, parityShardCount, offset, byteCount);
    }

    /**
     * Returns true if the parity shards contain the right data.
     *
     * This method may be significantly faster than the one above that does
     * not use a temporary buffer.
     *
     * @param shards An array containing data shards followed by parity shards.
     *               Each shard is a byte array, and they must all be the same
     *               size.
     * @param firstByte The index of the first byte in each shard to check.
     * @param byteCount The number of bytes to check in each shard.
     * @param tempBuffer A temporary buffer (the same size as each of the
     *                   shards) to use when computing parity.
     */
    public boolean isParityCorrect(byte[][] shards, int firstByte, int byteCount, byte [] tempBuffer) {
        checkBuffersAndSizes(shards, firstByte, byteCount);
        if (tempBuffer.length < firstByte + byteCount) {
            throw new IllegalArgumentException("tempBuffer is not big enough");
        }

        byte [] [] toCheck = new byte [parityShardCount] [];
        System.arraycopy(shards, dataShardCount, toCheck, 0, parityShardCount);
        return codingLoop.checkSomeShards(parityRows, shards, dataShardCount, toCheck, parityShardCount, firstByte, byteCount, tempBuffer);
    }

    /**
     * Given a list of shards, some of which contain data, fills in the
     * ones that don't have data.
     *
     * Quickly does nothing if all of the shards are present.
     *
     * If any shards are missing (based on the flags in shardsPresent),
     * the data in those shards is recomputed and filled in.
     */
    public void decodeMissing(ByteBuf [] shards, boolean [] shardPresent, final int offset, final int byteCount) {
        checkBuffersAndSizes(shards, offset, byteCount);
        int numberPresent = 0;
        for (int i = 0; i < totalShardCount; i++) {
            if (shardPresent[i]) {
                numberPresent += 1;
            }
        }
        if (numberPresent == totalShardCount) {
            return;
        }

        if (numberPresent < dataShardCount) {
            throw new IllegalArgumentException("Not enough shards present");
        }

        Matrix subMatrix = new Matrix(dataShardCount, dataShardCount);
        ByteBuf [] subShards = new ByteBuf[dataShardCount];
        {
            int subMatrixRow = 0;
            for (int matrixRow = 0; matrixRow < totalShardCount && subMatrixRow < dataShardCount; matrixRow++) {
                if (shardPresent[matrixRow]) {
                    for (int c = 0; c < dataShardCount; c++) {
                        subMatrix.set(subMatrixRow, c, matrix.get(matrixRow, c));
                    }
                    subShards[subMatrixRow] = shards[matrixRow];
                    subMatrixRow += 1;
                }
            }
        }

        Matrix dataDecodeMatrix = subMatrix.invert();
        ByteBuf [] outputs = new ByteBuf[parityShardCount];
        byte [] [] matrixRows = new byte [parityShardCount] [];
        int outputCount = 0;
        for (int iShard = 0; iShard < dataShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = dataDecodeMatrix.getRow(iShard);
                outputCount += 1;
            }
        }

        LOOP.codeSomeShards(matrixRows, subShards, dataShardCount, outputs, outputCount, offset, byteCount);

        outputCount = 0;
        for (int iShard = dataShardCount; iShard < totalShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = parityRows[iShard - dataShardCount];
                outputCount += 1;
            }
        }

        LOOP.codeSomeShards(matrixRows, shards, dataShardCount, outputs, outputCount, offset, byteCount);
    }

    /**
     * Checks the consistency of arguments passed to public methods.
     */
    private void checkBuffersAndSizes(ByteBuf [] shards, int offset, int byteCount) {
        if (shards.length != totalShardCount) {
            throw new IllegalArgumentException("wrong number of shards: " + shards.length);
        }

        int shardLength = shards[0].readableBytes();
        for (int i = 1; i < shards.length; i++) {
            if (shards[i].readableBytes() != shardLength) {
                throw new IllegalArgumentException("Shards are different sizes");
            }
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount is negative: " + byteCount);
        }
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("buffers to small: " + byteCount + offset);
        }
    }

    /**
     * Checks the consistency of arguments passed to public methods.
     */
    private void checkBuffersAndSizes(byte [] [] shards, int offset, int byteCount) {
        if (shards.length != totalShardCount) {
            throw new IllegalArgumentException("wrong number of shards: " + shards.length);
        }

        int shardLength = 0;
        boolean allShardIsEmpty = true;
        for (int i = 1; i < shards.length; i++) {
            if(shards[i]==null) {
                continue;
            }
            allShardIsEmpty = false;
            if(shardLength==0){
                shardLength = shards[i].length;
                continue;
            }
            if (shards[i].length != shardLength) {
                throw new IllegalArgumentException("Shards are different sizes");
            }
        }

        if(allShardIsEmpty) {
            throw new IllegalArgumentException("Shards are empty");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount is negative: " + byteCount);
        }
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("buffers to small: " + byteCount + offset);
        }
    }

    /**
     * Create the matrix to use for encoding, given the number of
     * data shards and the number of total shards.
     *
     * The top square of the matrix is guaranteed to be an identity
     * matrix, which means that the data shards are unchanged after
     * encoding.
     */
    private static Matrix buildMatrix(int dataShards, int totalShards) {
        Matrix vandermonde = vandermonde(totalShards, dataShards);
        Matrix top = vandermonde.submatrix(0, 0, dataShards, dataShards);
        return vandermonde.times(top.invert());
    }

    /**
     * Create a Vandermonde matrix, which is guaranteed to have the
     * property that any subset of rows that forms a square matrix
     * is invertible.
     *
     * @param rows Number of rows in the result.
     * @param cols Number of columns in the result.
     * @return A Matrix.
     */
    private static Matrix vandermonde(int rows, int cols) {
        Matrix result = new Matrix(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result.set(r, c, Galois.exp((byte) r, c));
            }
        }
        return result;
    }
}