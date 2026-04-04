package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneEntityAppearNotifyOuterClass.SceneEntityAppearNotify;

public final class SendSceneEntityAppearNotify implements SendPacket {
    private final byte[] data;

    public SendSceneEntityAppearNotify(Entity entity) {
        var proto =
            SceneEntityAppearNotify.newBuilder()
                .setAppearType(SceneEntityAppearNotify.VisionType.VISION_BORN)
                .addEntityList(entity.toProto())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSceneEntityAppearNotify(Entity entity, SceneEntityAppearNotify.VisionType visionType) {
        var proto =
            SceneEntityAppearNotify.newBuilder()
                .setAppearType(visionType)
                .addEntityList(entity.toProto())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneEntityAppearNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}