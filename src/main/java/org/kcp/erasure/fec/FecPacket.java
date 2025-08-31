package org.kcp.erasure.fec;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.util.Recycler;
import lombok.Getter;
import lombok.Setter;

public class FecPacket {
    @Getter private long seqid;
    @Getter @Setter private int flag;
    @Getter @Setter private ByteBuf data;
    private final Recycler.Handle<FecPacket> recyclerHandle;

    private static final Recycler<FecPacket> FEC_PACKET_RECYCLER = new Recycler<>() {
        @Override
        protected FecPacket newObject(Handle<FecPacket> handle) {
            return new FecPacket(handle);
        }
    };

    public static FecPacket newFecPacket(ByteBuf byteBuf){
        FecPacket pkt = FEC_PACKET_RECYCLER.get();
        pkt.seqid =byteBuf.readUnsignedIntLE();
        pkt.flag = byteBuf.readUnsignedShortLE();
        pkt.data = byteBuf.retainedSlice(byteBuf.readerIndex(),byteBuf.capacity()-byteBuf.readerIndex());
        pkt.data.writerIndex(byteBuf.readableBytes());
        return pkt;
    }

    private FecPacket(Recycler.Handle<FecPacket> recyclerHandle) {
        this.recyclerHandle = recyclerHandle;
    }

    public void release(){
        this.seqid = 0;
        this.flag = 0;
        this.data.release();
        this.data = null;
        recyclerHandle.recycle(this);
    }

    @Override
    public String toString() {
        return "FecPacket{" + "seqid=" + seqid + ", flag=" + flag + '}';
    }
}