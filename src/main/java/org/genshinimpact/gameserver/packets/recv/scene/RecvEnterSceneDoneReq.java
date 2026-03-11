package org.genshinimpact.gameserver.packets.recv.scene;

import com.google.protobuf.InvalidProtocolBufferException;
import org.generated.protobuf.EnterSceneDoneReqOuterClass.EnterSceneDoneReq;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.scene.SendEnterSceneDoneRsp;

public class RecvEnterSceneDoneReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        EnterSceneDoneReq req = EnterSceneDoneReq.parseFrom(packet.getData());

        System.out.println("EnterSceneDoneReq reached");

        session.sendPacket(new SendEnterSceneDoneRsp(req.getEnterSceneToken()));
    }

    @Override
    public int getCode() {
        return 277;
    }
}
