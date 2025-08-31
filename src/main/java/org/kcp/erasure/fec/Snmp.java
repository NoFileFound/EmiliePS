package org.kcp.erasure.fec;

// Imports
import java.util.concurrent.atomic.LongAdder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Snmp {
    public LongAdder BytesSent = new LongAdder();
    public LongAdder BytesReceived = new LongAdder();
    public LongAdder MaxConn = new LongAdder();
    public LongAdder ActiveOpens = new LongAdder();
    public LongAdder PassiveOpens = new LongAdder();
    public LongAdder CurrEstab = new LongAdder();
    public LongAdder InErrs = new LongAdder();
    public LongAdder InCsumErrors = new LongAdder();
    public LongAdder KCPInErrors = new LongAdder();
    public LongAdder InPkts = new LongAdder();
    public LongAdder OutPkts = new LongAdder();
    public LongAdder InSegs = new LongAdder();
    public LongAdder OutSegs = new LongAdder();
    public LongAdder InBytes = new LongAdder();
    public LongAdder OutBytes = new LongAdder();
    public LongAdder RetransSegs = new LongAdder();
    public LongAdder FastRetransSegs = new LongAdder();
    public LongAdder EarlyRetransSegs = new LongAdder();
    public LongAdder LostSegs = new LongAdder();
    public LongAdder RepeatSegs = new LongAdder();
    public LongAdder FECRecovered = new LongAdder();
    public LongAdder FECErrs = new LongAdder();
    public LongAdder FECDataShards = new LongAdder();
    public LongAdder FECParityShards = new LongAdder();
    public LongAdder FECShortShards = new LongAdder();
    public LongAdder FECRepeatDataShards = new LongAdder();

    public static void setSnmp(Snmp snmp) {
        Snmp.snmp = snmp;
    }

    @Getter
    public static volatile Snmp snmp = new Snmp();

    @Override
    public String toString() {
        return "Snmp{" + "BytesSent=" + BytesSent + ", BytesReceived=" + BytesReceived + ", MaxConn=" + MaxConn + ", ActiveOpens=" + ActiveOpens + ", PassiveOpens=" + PassiveOpens + ", CurrEstab=" + CurrEstab + ", InErrs=" + InErrs + ", InCsumErrors=" + InCsumErrors + ", KCPInErrors=" + KCPInErrors + ", 收到包=" + InPkts + ", 发送包=" + OutPkts + ", InSegs=" + InSegs + ", OutSegs=" + OutSegs + ", 收到字节=" + InBytes + ", 发送字节=" + OutBytes + ", 总共重发数=" + RetransSegs + ", 快速重发数=" + FastRetransSegs + ", 空闲快速重发数=" + EarlyRetransSegs + ", 超时重发数=" + LostSegs + ", 收到重复包数量=" + RepeatSegs + ", fec恢复数=" + FECRecovered + ", fec恢复错误数=" + FECErrs + ", 收到fecData数=" + FECDataShards + ", 收到fecParity数=" + FECParityShards + ", fec缓存冗余淘汰data包数=" + FECShortShards + ", fec收到重复的数据包=" + FECRepeatDataShards + '}';
    }
}