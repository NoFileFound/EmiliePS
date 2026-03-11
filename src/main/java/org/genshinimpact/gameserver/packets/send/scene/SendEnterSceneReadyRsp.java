package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.EnterSceneReadyRspOuterClass.EnterSceneReadyRsp;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendEnterSceneReadyRsp extends OutboundPacket {
    public SendEnterSceneReadyRsp(int sceneToken) {
        super(209);

        EnterSceneReadyRsp proto = EnterSceneReadyRsp.newBuilder()
                .setEnterSceneToken(sceneToken)
                .setRetcode(0)
                .build();

        this.setData(proto.toByteArray());
    }
}