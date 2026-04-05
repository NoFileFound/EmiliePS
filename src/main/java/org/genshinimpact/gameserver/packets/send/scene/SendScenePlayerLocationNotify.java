package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerLocationInfoOuterClass.PlayerLocationInfo;
import org.generated.protobuf.ScenePlayerLocationNotifyOuterClass.ScenePlayerLocationNotify;

public final class SendScenePlayerLocationNotify implements SendPacket {
    private final byte[] data;

    public SendScenePlayerLocationNotify(Scene scene) {
        var proto = ScenePlayerLocationNotify.newBuilder().setSceneId(scene.getSceneId());
        for(var playerEntry : scene.getPlayers()) {
            proto.addPlayerLocList(
                PlayerLocationInfo.newBuilder()
                    .setUid(playerEntry.getAccount().getId().intValue())
                    .setPos(playerEntry.getAccount().getPlayerPosition().toProto())
                    .setRot(playerEntry.getAccount().getPlayerRotation().toProto())
                    .build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ScenePlayerLocationNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}