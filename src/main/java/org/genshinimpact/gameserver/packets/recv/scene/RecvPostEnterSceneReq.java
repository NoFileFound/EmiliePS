package org.genshinimpact.gameserver.packets.recv.scene;

import com.google.protobuf.InvalidProtocolBufferException;
import org.generated.protobuf.PostEnterSceneReqOuterClass.PostEnterSceneReq;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.scene.SendPostEnterSceneRsp;

public class RecvPostEnterSceneReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        PostEnterSceneReq req = PostEnterSceneReq.parseFrom(packet.getData());

        System.out.println("PostEnterSceneReq reached");

        session.sendPacket(new SendPostEnterSceneRsp(req.getEnterSceneToken()));
    }

    @Override
    public int getCode() {
        return 3312;
    }
}