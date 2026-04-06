package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.VisionType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneEntityDisappearNotifyOuterClass.SceneEntityDisappearNotify;

public final class SendSceneEntityDisappearNotify implements SendPacket {
    private final byte[] data;

    public SendSceneEntityDisappearNotify(Entity entity, VisionType visionType) {
        var proto =
            SceneEntityDisappearNotify.newBuilder()
                .addEntityList(entity.getEntityId())
                .setDisappearType(visionType.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneEntityDisappearNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}