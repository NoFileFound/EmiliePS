package org.emilieps.game.packets.recv;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.game.connection.ClientSession;
import org.emilieps.game.packets.base.InboundPacket;
import org.emilieps.game.packets.base.PacketHandler;
import org.emilieps.game.packets.base.PacketOpcode;

// Packets
import org.emilieps.game.packets.send.PacketPingRsp;

// Protocol buffers
import generated.emilieps.protobuf.PingReqOuterClass.PingReq;

@SuppressWarnings("unused")
@PacketOpcode(PacketIdentifiers.Receive.PingReq)
public final class HandlerPingReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        PingReq req = PingReq.parseFrom(packet.getData());

        session.setLastPingTime(req.getClientTime());
        session.setLastFPS(req.getFps());
        session.sendPacket(new PacketPingRsp(req.getClientTime(), req.getSeq()));
    }
}