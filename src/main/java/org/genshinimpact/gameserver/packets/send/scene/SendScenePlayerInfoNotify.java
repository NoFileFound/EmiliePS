package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.ScenePlayerInfoNotifyOuterClass.ScenePlayerInfoNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendScenePlayerInfoNotify extends OutboundPacket {

    public SendScenePlayerInfoNotify() {
        super(267);

        ScenePlayerInfoNotify proto = ScenePlayerInfoNotify.newBuilder().build();

        this.setData(proto.toByteArray());
    }
}
