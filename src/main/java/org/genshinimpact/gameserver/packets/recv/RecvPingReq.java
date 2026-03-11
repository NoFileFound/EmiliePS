package org.genshinimpact.gameserver.packets.recv;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.SendPingRsp;

// Protocol buffers
import org.generated.protobuf.PingReqOuterClass.PingReq;

public class RecvPingReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        PingReq req = PingReq.parseFrom(packet.getData());

        session.sendPacket(new SendPingRsp(req.getClientTime(), req.getSeq()));
    }

    @Override
    public int getCode() {
        return 7;
    }
}