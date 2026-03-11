package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerLoginRsp;

// Protocol buffers
import org.generated.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneNotify;

public class RecvPlayerLoginReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        PlayerLoginReq req = PlayerLoginReq.parseFrom(packet.getData());

        session.sendPacket(new SendPlayerEnterSceneNotify("2"));
        session.sendPacket(new SendPlayerLoginRsp(req));




    }

    @Override
    public int getCode() {
        return 112;
    }
}