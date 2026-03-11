package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.PlayerEnterSceneNotifyOuterClass.PlayerEnterSceneNotify;
import org.generated.protobuf.VectorOuterClass;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendPlayerEnterSceneNotify extends OutboundPacket {
    public SendPlayerEnterSceneNotify(String uid) {
        super(272);

        PlayerEnterSceneNotify proto = PlayerEnterSceneNotify.newBuilder()
                .setSceneId(3)
                .setIsFirstLoginEnterScene(true)
                .setPos(VectorOuterClass.Vector.newBuilder().setX(2747).setY(194).setZ(-1719).build())
                .setSceneBeginTime((int)System.currentTimeMillis() / 1000)
                .setType(PlayerEnterSceneNotify.EnterType.ENTER_SELF)
                .setTargetUid(Integer.parseInt(uid))
                .setEnterSceneToken(69)
                .setEnterReason(PlayerEnterSceneNotify.EnterReason.ENTER_REASON_LOGIN)
                .setWorldLevel(0)
                .setWorldType(1)
                .setSceneTransaction("3-" + uid + "-" + System.currentTimeMillis() / 1000 + "-" + 18402)
                .build();

        this.setData(proto.toByteArray());
    }
}