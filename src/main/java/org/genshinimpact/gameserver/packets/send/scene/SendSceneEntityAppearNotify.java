package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.VisionType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneEntityAppearNotifyOuterClass.SceneEntityAppearNotify;

public final class SendSceneEntityAppearNotify implements SendPacket {
    private final byte[] data;

    public SendSceneEntityAppearNotify(Entity entity) {
        var proto =
            SceneEntityAppearNotify.newBuilder()
                .addEntityList(entity.toProto())
                .setAppearType(VisionType.VISION_BORN.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSceneEntityAppearNotify(Entity entity, VisionType visionType) {
        var proto =
            SceneEntityAppearNotify.newBuilder()
                .addEntityList(entity.toProto())
                .setAppearType(visionType.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSceneEntityAppearNotify(Entity entity, VisionType visionType, int param) {
        var proto =
            SceneEntityAppearNotify.newBuilder()
                .addEntityList(entity.toProto())
                .setAppearType(visionType.getValue())
                .setParam(param)
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