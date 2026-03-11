package org.genshinimpact.gameserver.packets.recv.scene;

import com.google.protobuf.InvalidProtocolBufferException;
import org.generated.protobuf.EnterSceneReadyReqOuterClass.EnterSceneReadyReq;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterScenePeerNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneReadyRsp;

public class RecvEnterSceneReadyReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        EnterSceneReadyReq proto = EnterSceneReadyReq.parseFrom(packet.getData());
        session.sendPacket(new SendEnterScenePeerNotify(proto.getEnterSceneToken()));
        session.sendPacket(new SendEnterSceneReadyRsp(proto.getEnterSceneToken()));
    }

    @Override
    public int getCode() {
        return 208;
    }
}