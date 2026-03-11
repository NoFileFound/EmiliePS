package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.PostEnterSceneRspOuterClass.PostEnterSceneRsp;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendPostEnterSceneRsp extends OutboundPacket {

    public SendPostEnterSceneRsp(int sceneToken) {
        super(3184);

        PostEnterSceneRsp proto = PostEnterSceneRsp.newBuilder().setEnterSceneToken(sceneToken).setRetcode(0).build();

        this.setData(proto.toByteArray());
    }
}
