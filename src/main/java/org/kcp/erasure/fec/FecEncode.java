package org.kcp.erasure.fec;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.kcp.erasure.IFecEncode;
import org.kcp.erasure.ReedSolomon;

public class FecEncode implements IFecEncode {
    private int dataShards;
    private int parityShards;
    private int shardSize;
    private long paws;
    private long next;
    private int shardCount;
    private int maxSize;
    private int headerOffset;
    private int payloadOffset;
    private final ByteBuf[] shardCache;
    private final ByteBuf[] encodeCache;
    private final ByteBuf zeros;
    private ReedSolomon codec;

    public FecEncode(int headerOffset, ReedSolomon codec,int mtu) {
        this.dataShards = codec.getDataShardCount();
        this.parityShards = codec.getParityShardCount();
        this.shardSize = this.dataShards + this.parityShards;
        this.paws = 0xffffffffL/shardSize * shardSize;
        this.headerOffset = headerOffset;
        this.payloadOffset = headerOffset + Fec.fecHeaderSize;
        this.codec =codec;
        this.shardCache = new ByteBuf[shardSize];
        this.encodeCache = new ByteBuf[parityShards];
        zeros = ByteBufAllocator.DEFAULT.buffer(mtu);
        zeros.writeBytes(new byte[mtu]);
    }

    public ByteBuf[] encode(final ByteBuf byteBuf){
        int headerOffset = this.headerOffset;
        int payloadOffset = this.payloadOffset;
        int dataShards = this.dataShards;
        int parityShards = this.parityShards;
        ByteBuf[] shardCache = this.shardCache;
        ByteBuf[] encodeCache = this.encodeCache;
        ByteBuf zeros = this.zeros;

        markData(byteBuf,headerOffset);
        int sz = byteBuf.writerIndex();
        byteBuf.setShort(payloadOffset,sz-headerOffset- Fec.fecHeaderSizePlus2);
        shardCache[shardCount] = byteBuf.retainedDuplicate();
        shardCount ++;
        if (sz > this.maxSize) {
            this.maxSize = sz;
        }
        if(shardCount!=dataShards) {
            return null;
        }

        for (int i = 0; i < parityShards; i++) {
            ByteBuf parityByte = ByteBufAllocator.DEFAULT.buffer(this.maxSize);
            shardCache[i+dataShards]  = parityByte;
            encodeCache[i] = parityByte;
            markParity(parityByte,headerOffset);
            parityByte.writerIndex(this.maxSize);
        }

        for (int i = 0; i < dataShards; i++) {
            ByteBuf shard = shardCache[i];
            int left = this.maxSize-shard.writerIndex();
            if(left<=0) {
                continue;
            }

            shard.writeBytes(zeros,left);
            zeros.readerIndex(0);
        }
        codec.encodeParity(shardCache,payloadOffset,this.maxSize-payloadOffset);
        for (int i = 0; i < dataShards; i++) {
            shardCache[i].release();
            shardCache[i]=null;
        }
        this.shardCount = 0;
        this.maxSize = 0;
        return encodeCache;
    }

    public void release(){
        this.dataShards = 0;
        this.parityShards = 0;
        this.shardSize = 0;
        this.paws = 0;
        this.next = 0;
        this.shardCount = 0;
        this.maxSize = 0;
        this.headerOffset = 0;
        this.payloadOffset = 0;
        zeros.release();
        codec=null;
    }

    private void markData(ByteBuf byteBuf,int offset){
        byteBuf.setIntLE(offset, (int) this.next);
        byteBuf.setShortLE(offset+4, Fec.typeData);
        this.next++;
    }

    private void markParity(ByteBuf byteBuf, int offset){
        byteBuf.setIntLE(offset, (int) this.next);
        byteBuf.setShortLE(offset + 4,Fec.typeParity);
        this.next = (this.next + 1) % this.paws;
    }
}