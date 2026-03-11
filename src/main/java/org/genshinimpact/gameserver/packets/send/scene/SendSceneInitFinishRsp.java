package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.SceneInitFinishRspOuterClass.SceneInitFinishRsp;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSceneInitFinishRsp extends OutboundPacket {
    public SendSceneInitFinishRsp(int sceneToken) {
        super(207);

        SceneInitFinishRsp proto = SceneInitFinishRsp.newBuilder()
                .setEnterSceneToken(sceneToken)
                .setRetcode(0)
                .build();

        this.setData(proto.toByteArray());
    }
}
