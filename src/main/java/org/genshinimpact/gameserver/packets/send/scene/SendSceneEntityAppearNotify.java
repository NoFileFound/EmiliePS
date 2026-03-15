package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.EntityClientDataOuterClass;
import org.generated.protobuf.SceneAvatarInfoOuterClass;
import org.generated.protobuf.SceneEntityAppearNotifyOuterClass.SceneEntityAppearNotify;
import org.generated.protobuf.SceneEntityInfoOuterClass;
import org.genshinimpact.gameserver.packets.SendPacket;

public class SendSceneEntityAppearNotify implements SendPacket {
    private final byte[] data;

    public SendSceneEntityAppearNotify(int entityId, int uid, int peerId, long guid) {
        var proto = SceneEntityAppearNotify.newBuilder()
                .setAppearType(SceneEntityAppearNotify.VisionType.VISION_BORN)
                .addEntityList(SceneEntityInfoOuterClass.SceneEntityInfo.newBuilder().setEntityId(entityId).setEntityType(SceneEntityInfoOuterClass.SceneEntityInfo.ProtEntityType.PROT_ENTITY_AVATAR).setEntityClientData(EntityClientDataOuterClass.EntityClientData.newBuilder()).setLifeState(1)
                        .setAvatar(SceneAvatarInfoOuterClass.SceneAvatarInfo.newBuilder()
                                .setGuid(guid)
                                .setAvatarId(entityId)
                                .setBornTime((int)System.currentTimeMillis() / 1000)
                                .setUid(uid)
                                .setPeerId(peerId).build()).build());

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return 221;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}
