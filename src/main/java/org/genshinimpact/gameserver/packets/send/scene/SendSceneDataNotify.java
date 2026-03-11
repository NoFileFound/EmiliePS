package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.SceneDataNotifyOuterClass.SceneDataNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSceneDataNotify extends OutboundPacket {

    public SendSceneDataNotify() {
        super(3203);

        this.setData(SceneDataNotify.newBuilder().build().toByteArray());
    }
}
