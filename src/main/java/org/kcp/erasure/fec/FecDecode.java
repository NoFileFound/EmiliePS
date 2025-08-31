package org.kcp.erasure.fec;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.kcp.erasure.IFecDecode;
import org.kcp.erasure.ReedSolomon;

public class FecDecode implements IFecDecode {
    private int rxlimit;
    private int dataShards;
    private int parityShards;
    private int shardSize;
    private final MyArrayList < FecPacket > rx;
    private final ByteBuf[] decodeCache;
    private final boolean[] flagCache;
    private final ByteBuf zeros;
    private ReedSolomon codec;

    public FecDecode(int rxlimit, ReedSolomon codec, int mtu) {
        this.rxlimit = rxlimit;
        this.dataShards = codec.getDataShardCount();
        this.parityShards = codec.getParityShardCount();
        this.shardSize = dataShards + parityShards;
        if (dataShards <= 0 || parityShards <= 0) {
            throw new FecException("dataShards and parityShards can not less than 0");
        }

        if (rxlimit < dataShards + parityShards) {
            throw new FecException("");
        }

        this.codec = codec;
        this.decodeCache = new ByteBuf[this.shardSize];
        this.flagCache = new boolean[this.shardSize];
        this.rx = new MyArrayList < > (rxlimit);

        zeros = ByteBufAllocator.DEFAULT.buffer(mtu);
        zeros.writeBytes(new byte[mtu]);
    }

    public List < ByteBuf > decode(final FecPacket pkt) {
        int shardSize = this.shardSize;
        MyArrayList < FecPacket > rx = this.rx;
        int dataShards = this.dataShards;
        ByteBuf zeros = this.zeros;
        int typeData = Fec.typeData;
        if (pkt.getFlag() == Fec.typeParity) {
            Snmp.snmp.FECParityShards.increment();
        } else {
            Snmp.snmp.FECDataShards.increment();
        }

        int n = rx.size() - 1;
        int insertIdx = 0;
        for (int i = n; i >= 0; i--) {
            if (pkt.getSeqid() == rx.get(i).getSeqid()) {
                Snmp.snmp.FECRepeatDataShards.increment();
                pkt.release();
                return null;
            }
            if (pkt.getSeqid() > rx.get(i).getSeqid()) {
                insertIdx = i + 1;
                break;
            }
        }

        if (insertIdx == n + 1) {
            rx.add(pkt);
        } else {
            rx.add(insertIdx, pkt);
        }

        long shardBegin = pkt.getSeqid() - pkt.getSeqid() % shardSize;
        long shardEnd = shardBegin + shardSize - 1;
        int searchBegin = (int)(insertIdx - pkt.getSeqid() % shardSize);
        if (searchBegin < 0) {
            searchBegin = 0;
        }

        int searchEnd = searchBegin + shardSize - 1;
        if (searchEnd >= rx.size()) {
            searchEnd = rx.size() - 1;
        }

        List < ByteBuf > result = null;
        if (searchEnd - searchBegin + 1 >= dataShards) {
            int numshard = 0;
            int numDataShard = 0;
            int first = 0;
            int maxlen = 0;
            ByteBuf[] shards = decodeCache;
            boolean[] shardsflag = flagCache;
            for (int i = 0; i < shards.length; i++) {
                shards[i] = null;
                shardsflag[i] = false;
            }

            for (int i = searchBegin; i <= searchEnd; i++) {
                FecPacket fecPacket = rx.get(i);
                long seqid = fecPacket.getSeqid();
                if (seqid > shardEnd) {
                    break;
                }
                if (seqid < shardBegin) {
                    continue;
                }
                shards[(int)(seqid % shardSize)] = fecPacket.getData();
                shardsflag[(int)(seqid % shardSize)] = true;
                numshard++;
                if (fecPacket.getFlag() == typeData) {
                    numDataShard++;
                }
                if (numshard == 1) {
                    first = i;
                }
                if (fecPacket.getData().readableBytes() > maxlen) {
                    maxlen = fecPacket.getData().readableBytes();
                }
            }

            if (numDataShard == dataShards) {
                freeRange(first, numshard, rx);
            } else if (numshard >= dataShards) {
                for (int i = 0; i < shards.length; i++) {
                    ByteBuf shard = shards[i];
                    if (shard == null) {
                        shards[i] = zeros.copy(0, maxlen);
                        shards[i].writerIndex(maxlen);
                        continue;
                    }
                    int left = maxlen - shard.readableBytes();
                    if (left > 0) {
                        shard.writeBytes(zeros, left);
                        zeros.resetReaderIndex();
                    }
                }
                codec.decodeMissing(shards, shardsflag, 0, maxlen);
                result = new ArrayList < > (dataShards);
                for (int i = 0; i < shardSize; i++) {
                    if (shardsflag[i]) {
                        continue;
                    }
                    ByteBuf byteBufs = shards[i];
                    if (i >= dataShards) {
                        byteBufs.release();
                        continue;
                    }

                    int packageSize = byteBufs.readShort();
                    if (byteBufs.readableBytes() < packageSize) {
                        System.out.println("bytebuf length: " + byteBufs.writerIndex() + " | pkg length" + packageSize);
                        byte[] bytes = new byte[byteBufs.writerIndex()];
                        byteBufs.getBytes(0, bytes);
                        for (byte aByte: bytes) {
                            System.out.print("[" + aByte + "] ");
                        }
                        Snmp.snmp.FECErrs.increment();
                    } else {
                        Snmp.snmp.FECRecovered.increment();
                    }
                    byteBufs = byteBufs.slice(Fec.fecDataSize, packageSize);
                    result.add(byteBufs);
                    Snmp.snmp.FECRecovered.increment();
                }
                freeRange(first, numshard, rx);
            }
        }
        if (rx.size() > rxlimit) {
            if (rx.getFirst().getFlag() == Fec.typeData) {
                Snmp.snmp.FECShortShards.increment();
            }
            freeRange(0, 1, rx);
        }
        return result;
    }

    public void release() {
        this.rxlimit = 0;
        this.dataShards = 0;
        this.parityShards = 0;
        this.shardSize = 0;
        for (FecPacket fecPacket: this.rx) {
            if (fecPacket == null) {
                continue;
            }
            fecPacket.release();
        }
        this.zeros.release();
        codec = null;
    }

    private static void freeRange(int first, int n, MyArrayList < FecPacket > q) {
        int toIndex = first + n;
        for (int i = first; i < toIndex; i++) {
            q.get(i).release();
        }
        q.removeRange(first, toIndex);
    }

    public static MyArrayList < Integer > build(int size) {
        MyArrayList < Integer > q = new MyArrayList < > (size);
        for (int i = 0; i < size; i++) {
            q.add(i);
        }
        return q;
    }
}