package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.EnterSceneDoneRspOuterClass.EnterSceneDoneRsp;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendEnterSceneDoneRsp extends OutboundPacket {

    public SendEnterSceneDoneRsp(int sceneToken) {
        super(237);

        EnterSceneDoneRsp proto = EnterSceneDoneRsp.newBuilder()
                .setRetcode(0)
                .setEnterSceneToken(sceneToken)
                .build();

        this.setData(proto.toByteArray());
    }
}