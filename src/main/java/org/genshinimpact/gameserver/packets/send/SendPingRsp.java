package org.genshinimpact.gameserver.packets.send;

// Imports
import org.genshinimpact.gameserver.packets.OutboundPacket;

// Protocol buffers
import org.generated.protobuf.PingRspOuterClass.PingRsp;

public class SendPingRsp extends OutboundPacket {
    public SendPingRsp(int clientTime, int seq) {
        super(21);

        this.setData(PingRsp.newBuilder().setRetcode(0).setClientTime(clientTime).setSeq(seq).build().toByteArray());
    }
}