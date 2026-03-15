package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneTimeNotifyOuterClass.SceneTimeNotify;

public final class SendSceneTimeNotify implements SendPacket {
    private final byte[] data;

    public SendSceneTimeNotify(Scene scene) {
        var proto =
            SceneTimeNotify.newBuilder()
                .setSceneId(scene.getSceneId())
                .setIsPaused(scene.isPaused())
                .setSceneTime(scene.getSceneTime())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneTimeNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}