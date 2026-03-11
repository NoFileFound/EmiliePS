package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.SceneTimeNotifyOuterClass.SceneTimeNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSceneTimeNotify extends OutboundPacket {

    public SendSceneTimeNotify() {
        super(245);

        var proto =
                SceneTimeNotify.newBuilder()
                        .setSceneId(3)
                        .setSceneTime(0)
                        .setIsPaused(false)
                        .build();

        this.setData(proto.toByteArray());
    }
}
