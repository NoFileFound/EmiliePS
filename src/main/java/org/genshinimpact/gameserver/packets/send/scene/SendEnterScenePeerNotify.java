package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.EnterScenePeerNotifyOuterClass.EnterScenePeerNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendEnterScenePeerNotify extends OutboundPacket {
    public SendEnterScenePeerNotify(int sceneToken) {
        super(252);

        EnterScenePeerNotify proto = EnterScenePeerNotify.newBuilder()
                .setEnterSceneToken(sceneToken)
                .setPeerId(0)
                .setDestSceneId(3)
                .setHostPeerId(0)
                .build();

        this.setData(proto.toByteArray());
    }
}
