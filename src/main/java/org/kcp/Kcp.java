package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.Recycler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lombok.Getter;
import lombok.Setter;
import net.openhft.hashing.LongHashFunction;
import org.kcp.erasure.fec.Snmp;
import org.kcp.internal.ReItrLinkedList;
import org.kcp.internal.ReusableListIterator;

public class Kcp implements IKcp {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Kcp.class);
    public static final boolean enableKcpV2 = false;
    public static final int IKCP_RTO_NDL = 30;
    public static final int IKCP_RTO_MIN = 100;
    public static final int IKCP_RTO_DEF = 200;
    public static final int IKCP_RTO_MAX = 60000;
    public static final byte IKCP_CMD_PUSH = 81;
    public static final byte IKCP_CMD_ACK = 82;
    public static final byte IKCP_CMD_WASK = 83;
    public static final byte IKCP_CMD_WINS = 84;
    public static final int IKCP_ASK_SEND = 1;
    public static final int IKCP_ASK_TELL = 2;
    public static final int IKCP_WND_SND = 256;
    public static final int IKCP_WND_RCV = 256;
    public static final int IKCP_MTU_DEF = 1400;
    public static final int IKCP_INTERVAL = 100;
    public int IKCP_OVERHEAD = (enableKcpV2 ? 32 : 28);
    public static final int IKCP_DEADLINK = 20;
    public static final int IKCP_THRESH_INIT = 2;
    public static final int IKCP_THRESH_MIN = 2;
    public static final int IKCP_PROBE_INIT = 7000;
    public static final int IKCP_PROBE_LIMIT = 120000;
    public static final int IKCP_SN_OFFSET   = 16;
    private int ackMaskSize = 0;
    private long conv;
    private int mtu = IKCP_MTU_DEF;
    private int mss = this.mtu - IKCP_OVERHEAD;
    private int state;
    private long sndUna;
    private long sndNxt;
    private long rcvNxt;
    private long tsLastack;
    private int ssthresh = IKCP_THRESH_INIT;
    private int rxRttval;
    private int rxSrtt;
    private int rxRto = IKCP_RTO_DEF;
    private int rxMinrto = IKCP_RTO_MIN;
    private int sndWnd = IKCP_WND_SND;
    private int rcvWnd = IKCP_WND_RCV;
    private int rmtWnd = IKCP_WND_RCV;
    private int cwnd;
    private int probe;
    private int interval = IKCP_INTERVAL;
    private long tsFlush = IKCP_INTERVAL;
    private boolean nodelay;
    private boolean updated;
    private long tsProbe;
    private int probeWait;
    private int deadLink = IKCP_DEADLINK;
    private int incr;
    private boolean ackNoDelay;
    private final LinkedList<Segment> sndQueue = new LinkedList<>();
    private final ReItrLinkedList<Segment> sndBuf = new ReItrLinkedList<>();
    private final ReItrLinkedList<Segment> rcvQueue = new ReItrLinkedList<>();
    private final ReItrLinkedList<Segment> rcvBuf = new ReItrLinkedList<>();
    private final ReusableListIterator<Segment> rcvQueueItr = rcvQueue.listIterator();
    public ReusableListIterator<Segment> sndBufItr = sndBuf.listIterator();
    private final ReusableListIterator<Segment> rcvBufItr = rcvBuf.listIterator();
    private long[] acklist = new long[8];
    private int ackcount;
    private User user;
    private int fastresend;
    private boolean nocwnd;
    private boolean stream;
    private int reserved;
    private KcpOutput output;
    private ByteBufAllocator byteBufAllocator = ByteBufAllocator.DEFAULT;
    private long ackMask;
    private long lastRcvNxt;
    private long startTicks = System.currentTimeMillis();

    private static long long2Uint(long n) {
        return n & 0x00000000FFFFFFFFL;
    }

    private static int ibound(int lower, int middle, int upper) {
        return Math.min(Math.max(lower, middle), upper);
    }

    private static int itimediff(long later, long earlier) {
        return (int)(later - earlier);
    }

    private static void output(ByteBuf data, Kcp kcp) {
        if(log.isDebugEnabled()) {
            log.debug("{} [RO] {} bytes", kcp, data.readableBytes());
        }

        if(data.readableBytes() == 0) {
            return;
        }

        kcp.output.out(data, kcp);
    }

    private static int encodeSeg(ByteBuf buf, Segment seg) {
        int offset = buf.writerIndex();
        buf.writeLong(seg.conv);
        buf.writeByte(seg.cmd);
        buf.writeByte(seg.frg);
        buf.writeShortLE(seg.wnd);
        buf.writeIntLE((int)seg.ts);
        buf.writeIntLE((int)seg.sn);
        buf.writeIntLE((int)seg.una);
        int dataSize = seg.data == null ? 0 : seg.data.readableBytes();
        buf.writeIntLE(dataSize);
        switch(seg.ackMaskSize) {
            case 8:
                buf.writeByte((int) seg.ackMask);
                 break;
            case 16:
                buf.writeShortLE((int) seg.ackMask);
                break;
            case 32:
                buf.writeIntLE((int) seg.ackMask);
                break;
            case 64:
                buf.writeLongLE(seg.ackMask);
                break;
        }

        if(enableKcpV2) buf.writeIntLE(seg.byte_check_code);
        Snmp.snmp.OutSegs.increment();
        return buf.writerIndex() - offset;
    }

    public Kcp(long conv, KcpOutput output) {
        this.conv = conv;
        this.output = output;
    }

    @Override
    public void release() {
        release(sndBuf);
        release(rcvBuf);
        release(sndQueue);
        release(rcvQueue);
    }

    private void release(List<Segment> segQueue) {
        for(Segment seg : segQueue) {
            seg.recycle(true);
        }
    }

    private ByteBuf createFlushByteBuf() {
        return byteBufAllocator.ioBuffer(this.mtu);
    }

    @Override
    public ByteBuf mergeRecv() {
        if(rcvQueue.isEmpty()) {
            return null;
        }

        int peekSize = peekSize();
        if(peekSize < 0) {
            return null;
        }

        boolean recover = false;
        if(rcvQueue.size() >= rcvWnd) {
            recover = true;
        }

        ByteBuf byteBuf = null;
        int len = 0;
        for(Iterator<Segment> itr = rcvQueueItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            len += seg.data.readableBytes();
            int fragment = seg.frg;
            itr.remove();
            if(log.isDebugEnabled()) {
                log.debug("{} recv sn={}", this, seg.sn);
            }

            if(byteBuf == null) {
                if(fragment == 0) {
                    byteBuf = seg.data;
                    seg.recycle(false);
                    break;
                }

                byteBuf = byteBufAllocator.ioBuffer(len);
            }

            byteBuf.writeBytes(seg.data);
            seg.recycle(true);
            if(fragment == 0) {
                break;
            }
        }

        assert len == peekSize;
        moveRcvData();
        if(rcvQueue.size() < rcvWnd && recover) {
            probe |= IKCP_ASK_TELL;
        }

        return byteBuf;
    }

    @Override
    public int recv(List<ByteBuf> bufList) {
        if(rcvQueue.isEmpty()) {
            return -1;
        }

        int peekSize = peekSize();
        if(peekSize < 0) {
            return -2;
        }

        boolean recover = false;
        if(rcvQueue.size() >= rcvWnd) {
            recover = true;
        }

        int len = 0;
        for(Iterator<Segment> itr = rcvQueueItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            len += seg.data.readableBytes();
            bufList.add(seg.data);
            int fragment = seg.frg;
            if(log.isDebugEnabled()) {
                log.debug("{} recv sn={}", this, seg.sn);
            }

            itr.remove();
            seg.recycle(false);

            if(fragment == 0) {
                break;
            }
        }

        assert len == peekSize;
        moveRcvData();
        if(rcvQueue.size() < rcvWnd && recover) {
            probe |= IKCP_ASK_TELL;
        }

        return len;
    }

    @Override
    public int peekSize() {
        if(rcvQueue.isEmpty()) {
            return -1;
        }

        Segment seg = rcvQueue.peek();
        if(seg.frg == 0) {
            return seg.data.readableBytes();
        }

        if(rcvQueue.size() < seg.frg + 1) {
            return -1;
        }

        int len = 0;
        for(Iterator<Segment> itr = rcvQueueItr.rewind(); itr.hasNext(); ) {
            Segment s = itr.next();
            len += s.data.readableBytes();
            if(s.frg == 0) {
                break;
            }
        }

        return len;
    }

    @Override
    public boolean canRecv() {
        if(rcvQueue.isEmpty()) {
            return false;
        }

        Segment seg = rcvQueue.peek();
        if(seg.frg == 0) {
            return true;
        }

        if(rcvQueue.size() < seg.frg + 1) {
            return false;
        }

        return true;
    }

    @Override
    public int send(ByteBuf buf) {
        assert mss > 0;
        int len = buf.readableBytes();
        if(len == 0) {
            return -1;
        }

        if(stream) {
            if(!sndQueue.isEmpty()) {
                Segment last = sndQueue.peekLast();
                ByteBuf lastData = last.data;
                int lastLen = lastData.readableBytes();
                if(lastLen < mss) {
                    int capacity = mss - lastLen;
                    int extend = len < capacity ? len : capacity;
                    if(lastData.maxWritableBytes() < extend) { // extend
                        ByteBuf newBuf = byteBufAllocator.ioBuffer(lastLen + extend);
                        newBuf.writeBytes(lastData);
                        lastData.release();
                        lastData = last.data = newBuf;
                    }

                    lastData.writeBytes(buf, extend);
                    len = buf.readableBytes();
                    if(len == 0) {
                        return 0;
                    }
                }
            }
        }

        int count;
        if(len <= mss) {
            count = 1;
        } else {
            count = (len + mss - 1) / mss;
        }

        if(count > 255) {
            return -2;
        }

        if(count == 0) {
            count = 1;
        }

        for(int i = 0; i < count; i++) {
            int size = Math.min(len, mss);
            Segment seg = Segment.createSegment(buf.readRetainedSlice(size));
            seg.frg = (short) (stream ? 0 : count - i - 1);
            sndQueue.add(seg);
            len = buf.readableBytes();
        }

        return 0;
    }

    private void updateAck(int rtt) {
        if(rxSrtt == 0) {
            rxSrtt = rtt;
            rxRttval = rtt >> 2;
        } else {
            int delta = rtt - rxSrtt;
            rxSrtt += delta>>3;
            delta = Math.abs(delta);
            if(rtt < rxSrtt - rxRttval) {
                rxRttval += ( delta - rxRttval)>>5;
            } else {
                rxRttval += (delta - rxRttval) >>2;
            }
        }
        int rto = rxSrtt + Math.max(interval, rxRttval<<2);
        rxRto = ibound(rxMinrto, rto, IKCP_RTO_MAX);
    }

    private void shrinkBuf() {
        if(sndBuf.size() > 0) {
            Segment seg = sndBuf.peek();
            sndUna = seg.sn;
        } else {
            sndUna = sndNxt;
        }
    }

    private void parseAck(long sn) {
        if(itimediff(sn, sndUna) < 0 || itimediff(sn, sndNxt) >= 0) {
            return;
        }

        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            if(sn == seg.sn) {
                itr.remove();
                seg.recycle(true);
                break;
            }

            if(itimediff(sn, seg.sn) < 0) {
                break;
            }
        }
    }

    private int parseUna(long una) {
        int count = 0;
        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            if(itimediff(una, seg.sn) > 0) {
                count++;
                itr.remove();
                seg.recycle(true);
            } else {
                break;
            }
        }

        return count;
    }

    private void parseAckMask(long una,long ackMask) {
        if(ackMask == 0)
        {
            return;
        }
        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            long index = seg.sn-una-1;
            if(index < 0) {
                continue;
            }

            if(index >= ackMaskSize) {
                break;
            }

            long mask = ackMask & 1 << index;
            if(mask != 0) {
                itr.remove();
                seg.recycle(true);
            }
        }
    }

    private void parseFastack(long sn, long ts) {
        if(itimediff(sn, sndUna) < 0 || itimediff(sn, sndNxt) >= 0) {
            return;
        }

        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            if(itimediff(sn, seg.sn) < 0) {
                break;
            } else if(sn != seg.sn&& itimediff(seg.ts, ts) <= 0) {
                seg.fastack++;
            }
        }
    }

    private void ackPush(long sn, long ts) {
        int newSize = 2 * (ackcount + 1);
        if(newSize > acklist.length) {
            int newCapacity = acklist.length << 1; // double capacity
            if(newCapacity < 0) {
                throw new OutOfMemoryError();
            }

            long[] newArray = new long[newCapacity];
            System.arraycopy(acklist, 0, newArray, 0, acklist.length);
            this.acklist = newArray;
        }

        acklist[2 * ackcount] =  sn;
        acklist[2 * ackcount + 1] =  ts;
        ackcount++;
    }

    private boolean parseData(Segment newSeg) {
        long sn = newSeg.sn;
        if(itimediff(sn, rcvNxt + rcvWnd) >= 0 || itimediff(sn, rcvNxt) < 0) {
            newSeg.recycle(true);
            return true;
        }

        boolean repeat = false;
        boolean findPos = false;
        ListIterator<Segment> listItr = null;
        if(rcvBuf.size() > 0) {
            listItr = rcvBufItr.rewind(rcvBuf.size());
            while(listItr.hasPrevious()) {
                Segment seg = listItr.previous();
                if(seg.sn == sn) {
                    repeat = true;
                    break;
                }

                if(itimediff(sn, seg.sn) > 0) {
                    findPos = true;
                    break;
                }
            }
        }

        if(repeat) {
            newSeg.recycle(true);
        } else if(listItr == null) {
            rcvBuf.add(newSeg);
        } else {
            if(findPos) {
                listItr.next();
            }

            listItr.add(newSeg);
        }

        moveRcvData();
        return repeat;
    }

    private void moveRcvData() {
        for(Iterator<Segment> itr = rcvBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            if(seg.sn == rcvNxt && rcvQueue.size() < rcvWnd) {
                itr.remove();
                rcvQueue.add(seg);
                rcvNxt++;
            } else {
                break;
            }
        }
    }

    @Override
    public int input(ByteBuf data, boolean regular, long current) {
        long oldSndUna = sndUna;
        if(data == null || data.readableBytes() < IKCP_OVERHEAD) {
            return -1;
        }

        if(log.isDebugEnabled()) {
            log.debug("{} [RI] {} bytes", this, data.readableBytes());
        }

        long latest = 0;
        boolean flag = false;
        int inSegs = 0;
        boolean windowSlides = false;
        long uintCurrent = long2Uint(currentMs(current));
        while(true) {
            long conv;
            int len, wnd;
            long ts, sn, una,ackMask;
            byte cmd;
            short frg;
            Segment seg;
            int byte_check_code;
            if(data.readableBytes() < IKCP_OVERHEAD) {
                break;
            }

            conv = data.readLong();
            if(conv != this.conv) {
                return -4;
            }

            cmd = data.readByte();
            frg = data.readUnsignedByte();
            wnd = data.readUnsignedShortLE();
            ts = data.readIntLE();
            sn = data.readUnsignedIntLE();
            una = data.readUnsignedIntLE();
            len = data.readIntLE();
            if(enableKcpV2) byte_check_code = data.readIntLE();
            ackMask = switch(ackMaskSize) {
                case 8 -> data.readUnsignedByte();
                case 16 -> data.readUnsignedShortLE();
                case 32 -> data.readUnsignedIntLE();
                case 64 -> data.readLongLE();
                default -> 0;
            };

            if(data.readableBytes() < len || len < 0) {
                return -2;
            }

            if(cmd != IKCP_CMD_PUSH && cmd != IKCP_CMD_ACK && cmd != IKCP_CMD_WASK && cmd != IKCP_CMD_WINS) {
                return -3;
            }

            if(regular) {
                this.rmtWnd = wnd;
            }

            //this.rmtWnd = wnd;
            if(parseUna(una)>0)
            {
                windowSlides = true;
            }
            shrinkBuf();


            boolean readed = false;
            switch (cmd) {
                case IKCP_CMD_ACK: {
                    parseAck(sn);
                    parseFastack(sn,ts);
                    flag = true;
                    latest= ts;
                    int rtt = itimediff(uintCurrent, ts);
                    if(log.isDebugEnabled()) {
                        log.debug("{} input ack: sn={}, rtt={}, rto={} ,regular={} ts={}", this, sn, rtt, rxRto,regular,ts);
                    }

                    break;
                }
                case IKCP_CMD_PUSH: {
                    boolean repeat = true;
                    if(itimediff(sn, rcvNxt + rcvWnd) < 0) {
                        ackPush(sn, ts);
                        if(itimediff(sn, rcvNxt) >= 0) {
                            if(len > 0) {
                                seg = Segment.createSegment(data.readRetainedSlice(len));
                                readed = true;
                            } else {
                                seg = Segment.createSegment(byteBufAllocator, 0);
                            }

                            seg.conv = conv;
                            seg.cmd = cmd;
                            seg.frg = frg;
                            seg.wnd = wnd;
                            seg.ts = ts;
                            seg.sn = sn;
                            seg.una = una;
                            if(enableKcpV2) seg.byte_check_code = byte_check_code;
                            repeat = parseData(seg);
                        }
                    }

                    if(regular && repeat) {
                        Snmp.snmp.RepeatSegs.increment();
                    }

                    if(log.isDebugEnabled()) {
                        log.debug("{} input push: sn={}, una={}, ts={},regular={}", this, sn, una, ts,regular);
                    }

                    break;
                }
                case IKCP_CMD_WASK: {
                    probe |= IKCP_ASK_TELL;
                    if(log.isDebugEnabled()) {
                        log.debug("{} input ask", this);
                    }

                    break;
                }
                case IKCP_CMD_WINS: {
                    if(log.isDebugEnabled()) {
                        log.debug("{} input tell: {}", this, wnd);
                    }

                    break;
                }
                default: return -3;
            }

            parseAckMask(una,ackMask);
            if(!readed) {
                data.skipBytes(len);
            }

            inSegs++;
        }

        Snmp.snmp.InSegs.add(inSegs);
        if(flag && regular) {
            int rtt = itimediff(uintCurrent, latest);
            if(rtt >= 0) {
                updateAck(rtt);
            }
        }

        if(!nocwnd) {
            if(itimediff(sndUna, oldSndUna) > 0) {
                if(cwnd < rmtWnd) {
                    int mss = this.mss;
                    if(cwnd < ssthresh) {
                        cwnd++;
                        incr += mss;
                    } else {
                        if(incr < mss) {
                            incr = mss;
                        }
                        incr += (mss * mss) / incr + (mss / 16);
                        if((cwnd + 1) * mss <= incr) {
                            if(mss > 0) {
                                cwnd = (incr + mss - 1) / mss;
                            } else {
                                cwnd = incr + mss - 1;
                            }
                        }
                    }

                    if(cwnd > rmtWnd) {
                        cwnd = rmtWnd;
                        incr = rmtWnd * mss;
                    }
                }
            }
        }

        return 0;
    }

    private int wndUnused() {
        if(rcvQueue.size() < rcvWnd) {
            return rcvWnd - rcvQueue.size();
        }

        return 0;
    }

    private ByteBuf makeSpace(ByteBuf buffer ,int space) {
        if(buffer == null) {
            buffer = createFlushByteBuf();
            buffer.writerIndex(reserved);
        } else if(buffer.readableBytes() + space > mtu) {
            output(buffer, this);
            buffer = createFlushByteBuf();
            buffer.writerIndex(reserved);
        }

        return buffer;
    }

    private void flushBuffer(ByteBuf buffer) {
        if(buffer == null)
            return;

        if(buffer.readableBytes() > reserved) {
            output(buffer, this);
            return;
        }

        buffer.release();
    }


    @Override
    public long currentMs(long now) {
        return now - startTicks;
    }

    @Override
    public long flush(boolean ackOnly, long current) {
        current = currentMs(current);
        Segment seg = Segment.createSegment(byteBufAllocator, 0);
        seg.conv = conv;
        seg.cmd = IKCP_CMD_ACK;
        seg.ackMaskSize=this.ackMaskSize;
        seg.wnd = wndUnused();
        seg.una = rcvNxt;
        ByteBuf buffer = null;
        int count = ackcount;
        if(lastRcvNxt != rcvNxt) {
            ackMask = 0;
            lastRcvNxt = rcvNxt;
        }

        for(int i = 0; i < count; i++) {
            long sn =  acklist[i * 2];
            if(sn < rcvNxt) {
                continue;
            }

            long index = sn - rcvNxt-1;
            if(index >= ackMaskSize) {
                break;
            }

            if(index >= 0) {
                ackMask |= 1 << index;
            }
        }

        seg.ackMask = ackMask;
        for(int i = 0; i < count; i++) {
            long sn =  acklist[i * 2];
            if(itimediff(sn , rcvNxt)>=0 || count-1 == i) {
                buffer =  makeSpace(buffer,IKCP_OVERHEAD);
                seg.sn = sn;
                seg.ts = acklist[i * 2 + 1];
                encodeSeg(buffer, seg);
                if(log.isDebugEnabled()) {
                    log.debug("{} flush ack: sn={}, ts={} ,count={}", this, seg.sn, seg.ts,count);
                }
            }
        }

        ackcount = 0;
        if(ackOnly) {
            flushBuffer(buffer);
            seg.recycle(true);
            return interval;
        }

        if(rmtWnd == 0) {
            if(probeWait == 0) {
                probeWait = IKCP_PROBE_INIT;
                tsProbe = current + probeWait;
            } else {
                if(itimediff(current, tsProbe) >= 0) {
                    if(probeWait < IKCP_PROBE_INIT) {
                        probeWait = IKCP_PROBE_INIT;
                    }

                    probeWait += probeWait / 2;
                    if(probeWait > IKCP_PROBE_LIMIT) {
                        probeWait = IKCP_PROBE_LIMIT;
                    }

                    tsProbe = current + probeWait;
                    probe |= IKCP_ASK_SEND;
                }
            }
        } else {
            tsProbe = 0;
            probeWait = 0;
        }

        if((probe & IKCP_ASK_SEND) != 0) {
            seg.cmd = IKCP_CMD_WASK;
            buffer = makeSpace(buffer,IKCP_OVERHEAD);
            encodeSeg(buffer, seg);
            if(log.isDebugEnabled()) {
                log.debug("{} flush ask", this);
            }
        }

        if((probe & IKCP_ASK_TELL) != 0) {
            seg.cmd = IKCP_CMD_WINS;
            buffer = makeSpace(buffer,IKCP_OVERHEAD);
            encodeSeg(buffer, seg);
            if(log.isDebugEnabled()) {
                log.debug("{} flush tell: wnd={}", this, seg.wnd);
            }
        }

        probe = 0;
        int cwnd0 = Math.min(sndWnd, rmtWnd);
        if(!nocwnd) {
            cwnd0 = Math.min(this.cwnd, cwnd0);
        }

        int newSegsCount=0;
        while (itimediff(sndNxt, sndUna + cwnd0) < 0) {
            Segment newSeg = sndQueue.poll();
            if(newSeg == null) {
                break;
            }

            newSeg.conv = conv;
            newSeg.cmd = IKCP_CMD_PUSH;
            newSeg.sn = sndNxt;
            if(enableKcpV2) {
                LongHashFunction xxh3 = LongHashFunction.xx3();
                newSeg.byte_check_code = (int) xxh3.hashBytes(newSeg.data.nioBuffer());
            }
            sndBuf.add(newSeg);
            sndNxt++;
            newSegsCount++;
        }

        int resent = fastresend > 0 ? fastresend : Integer.MAX_VALUE;
        int change = 0;
        boolean lost = false;
        int lostSegs = 0, fastRetransSegs=0, earlyRetransSegs=0;
        long minrto = interval;
        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext();) {
            Segment segment = itr.next();
            boolean needsend = false;
            if(segment.xmit == 0) {
                needsend = true;
                segment.rto = rxRto;
                segment.resendts = current + segment.rto;
                if(log.isDebugEnabled()) {
                    log.debug("{} flush data: sn={}, resendts={}", this, segment.sn, (segment.resendts - current));
                }
            }  else if(segment.fastack >= resent) {
                needsend = true;
                segment.fastack = 0;
                segment.rto = rxRto;
                segment.resendts = current + segment.rto;
                change++;
                fastRetransSegs++;
                if(log.isDebugEnabled()) {
                    log.debug("{} fastresend. sn={}, xmit={}, resendts={} ", this, segment.sn, segment.xmit, (segment.resendts - current));
                }
            }
            else if(segment.fastack > 0 &&newSegsCount == 0) {
                needsend = true;
                segment.fastack = 0;
                segment.rto = rxRto;
                segment.resendts = current + segment.rto;
                change++;
                earlyRetransSegs++;
            }
            else if(itimediff(current, segment.resendts) >= 0) {
                needsend = true;
                if(!nodelay) {
                    segment.rto += rxRto;
                } else {
                    segment.rto += rxRto / 2;
                }

                segment.fastack = 0;
                segment.resendts = current + segment.rto;
                lost = true;
                lostSegs++;
                if(log.isDebugEnabled()) {
                    log.debug("{} resend. sn={}, xmit={}, resendts={}", this, segment.sn, segment.xmit, (segment.resendts - current));
                }
            }

            if(needsend) {
                segment.xmit++;
                segment.ts = long2Uint(current);
                segment.wnd = seg.wnd;
                segment.una = rcvNxt;
                segment.ackMaskSize = this.ackMaskSize;
                segment.ackMask = ackMask;
                ByteBuf segData = segment.data;
                int segLen = segData.readableBytes();
                int need = IKCP_OVERHEAD + segLen;
                buffer = makeSpace(buffer,need);
                encodeSeg(buffer, segment);
                if(segLen > 0) {
                    buffer.writeBytes(segData, segData.readerIndex(), segLen);
                }

                long rto = itimediff(segment.resendts, current);
                if(rto > 0 &&rto < minrto) {
                    minrto = rto;
                }
            }
        }

        flushBuffer(buffer);
        seg.recycle(true);
        int sum = lostSegs;
        if(lostSegs > 0) {
            Snmp.snmp.LostSegs.add(lostSegs);
        }

        if(fastRetransSegs > 0) {
            Snmp.snmp.FastRetransSegs.add(fastRetransSegs);
            sum += fastRetransSegs;
        }

        if(earlyRetransSegs > 0) {
            Snmp.snmp.EarlyRetransSegs.add(earlyRetransSegs);
            sum += earlyRetransSegs;
        }

        if(sum > 0) {
            Snmp.snmp.RetransSegs.add(sum);
        }

        if(!nocwnd) {
            if(change > 0) {
                int inflight = (int) (sndNxt - sndUna);
                ssthresh = inflight / 2;
                if(ssthresh < IKCP_THRESH_MIN) {
                    ssthresh = IKCP_THRESH_MIN;
                }

                cwnd = ssthresh + resent;
                incr = cwnd * mss;
            }

            if(lost) {
                ssthresh = cwnd0 / 2;
                if(ssthresh < IKCP_THRESH_MIN) {
                    ssthresh = IKCP_THRESH_MIN;
                }

                cwnd = 1;
                incr = mss;
            }

            if(cwnd < 1) {
                cwnd = 1;
                incr = mss;
            }
        }

        return minrto;
    }

    @Override
    public void update(long current) {
        if(!updated) {
            updated = true;
            tsFlush = current;
        }

        int slap = itimediff(current, tsFlush);
        if(slap >= 10000 || slap < -10000) {
            tsFlush = current;
            slap = 0;
        }

        if(slap >= 0) {
            tsFlush += interval;
            if(itimediff(current, tsFlush) >= 0) {
                tsFlush = current + interval;
            }
        } else {
            tsFlush = current + interval;
        }

        flush(false,current);
    }

    @Override
    public long check(long current) {
        if(!updated) {
            return current;
        }

        long tsFlush = this.tsFlush;
        int slap = itimediff(current, tsFlush);
        if(slap >= 10000 || slap < -10000) {
            tsFlush = current;
            slap = 0;
        }

        if(slap >= 0) {
            return current;
        }

        int tmFlush = itimediff(tsFlush, current);
        int tmPacket = Integer.MAX_VALUE;
        for(Iterator<Segment> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            Segment seg = itr.next();
            int diff = itimediff(seg.resendts, current);
            if(diff <= 0) {
                return current;
            }

            if(diff < tmPacket) {
                tmPacket = diff;
            }
        }

        int minimal = Math.min(tmPacket, tmFlush);
        if(minimal >= interval) {
            minimal = interval;
        }

        return current + minimal;
    }

    @Override
    public boolean checkFlush() {
        if(ackcount > 0) {
            return true;
        }

        if(probe != 0) {
            return true;
        }

        if(!sndBuf.isEmpty()) {
            return true;
        }

        if(!sndQueue.isEmpty()) {
            return true;
        }

        return false;
    }


    @Override
    public int setMtu(int mtu) {
        if(mtu < IKCP_OVERHEAD || mtu < 50) {
            return -1;
        }

        if(reserved >= mtu-IKCP_OVERHEAD || reserved < 0) {
            return -1;
        }

        this.mtu = mtu;
        this.mss = mtu - IKCP_OVERHEAD-reserved;
        return 0;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public int nodelay(boolean nodelay, int interval, int resend, boolean nc) {
        this.nodelay = nodelay;
        if(nodelay) {
            this.rxMinrto = IKCP_RTO_NDL;
        } else {
            this.rxMinrto = IKCP_RTO_MIN;
        }

        if(interval >= 0) {
            if(interval > 5000) {
                interval = 5000;
            } else if(interval < 10) {
                interval = 10;
            }
            this.interval = interval;
        }

        if(resend >= 0) {
            fastresend = resend;
        }

        this.nocwnd = nc;
        return 0;
    }

    @Override
    public int waitSnd() {
        return this.sndBuf.size() + this.sndQueue.size();
    }

    @Override
    public long getConv() {
        return conv;
    }

    @Override
    public void setConv(long conv) {
        this.conv = conv;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public boolean isNodelay() {
        return nodelay;
    }

    @Override
    public void setNodelay(boolean nodelay) {
        this.nodelay = nodelay;
        if(nodelay) {
            this.rxMinrto = IKCP_RTO_NDL;
        } else {
            this.rxMinrto = IKCP_RTO_MIN;
        }
    }


    @Override
    public void setFastresend(int fastresend) {
        this.fastresend = fastresend;
    }

    @Override
    public void setRxMinrto(int rxMinrto) {
        this.rxMinrto = rxMinrto;
    }

    @Override
    public void setRcvWnd(int rcvWnd) {
        this.rcvWnd = rcvWnd;
    }

    @Override
    public void setAckMaskSize(int ackMaskSize) {
        this.ackMaskSize = ackMaskSize;
        this.IKCP_OVERHEAD+=(ackMaskSize/8);
        this.mss = mtu - IKCP_OVERHEAD-reserved;
    }

    @Override
    public void setReserved(int reserved) {
        this.reserved = reserved;
        this.mss = mtu - IKCP_OVERHEAD-reserved;
    }


    @Override
    public int getSndWnd() {
        return sndWnd;
    }

    @Override
    public void setSndWnd(int sndWnd) {
        this.sndWnd = sndWnd;
    }

    @Override
    public boolean isStream() {
        return stream;
    }

    @Override
    public void setStream(boolean stream) {
        this.stream = stream;
    }

    @Override
    public void setByteBufAllocator(ByteBufAllocator byteBufAllocator) {
        this.byteBufAllocator = byteBufAllocator;
    }

    @Override
    public KcpOutput getOutput() {
        return output;
    }

    @Override
    public void setOutput(KcpOutput output) {
        this.output = output;
    }


    @Override
    public void setAckNoDelay(boolean ackNoDelay) {
        this.ackNoDelay = ackNoDelay;
    }

    @Override
    public int getSrtt() {
        return rxSrtt;
    }

    @Override
    public String toString() {
        return "Kcp(" + "conv=" + conv + ')';
    }

    public static class Segment {
        private final Recycler.Handle<Kcp.Segment> recyclerHandle;
        private long conv;
        private byte cmd;
        private short frg;
        private int wnd;
        private long ts;
        private long sn;
        private long una;
        @Setter @Getter private long resendts;
        private int rto;
        private int fastack;
        @Setter @Getter private int xmit;
        private int byte_check_code;
        private long ackMask;
        private ByteBuf data;
        private int ackMaskSize;
        private static final Recycler<Kcp.Segment> RECYCLER = new Recycler<Kcp.Segment>() {
            @Override
            protected Segment newObject(Recycler.Handle<Segment> handle) {
                return new Segment(handle);
            }
        };

        private Segment(Recycler.Handle<Kcp.Segment> recyclerHandle) {
            this.recyclerHandle = recyclerHandle;
        }

        void recycle(boolean releaseBuf) {
            conv = 0;
            cmd = 0;
            frg = 0;
            wnd = 0;
            ts = 0;
            sn = 0;
            una = 0;
            resendts = 0;
            rto = 0;
            fastack = 0;
            xmit = 0;
            ackMask=0;
            byte_check_code = 0;
            if(releaseBuf&&data!=null) {
                data.release();
            }

            data = null;
            recyclerHandle.recycle(this);
        }

        static Segment createSegment(ByteBufAllocator byteBufAllocator, int size) {
            Segment seg = RECYCLER.get();
            if(size == 0) {
                seg.data = null;
            } else {
                seg.data = byteBufAllocator.ioBuffer(size);
            }

            return seg;
        }

        public static Segment createSegment(ByteBuf buf) {
            Segment seg = RECYCLER.get();
            seg.data = buf;
            return seg;
        }
    }
}