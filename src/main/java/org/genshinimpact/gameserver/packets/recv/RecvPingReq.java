package org.genshinimpact.gameserver.packets.recv;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.SendPingRsp;

// Protocol buffers
import org.generated.protobuf.PingReqOuterClass.PingReq;

public final class RecvPingReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = PingReq.parseFrom(data);
        var sequenceId = req.getSeq();
        session.sendPacket(new SendPingRsp(req.getClientTime(), sequenceId), sequenceId);
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PingReq;
    }
}