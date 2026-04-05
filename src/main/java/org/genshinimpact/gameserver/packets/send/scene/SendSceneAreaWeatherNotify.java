package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneAreaWeatherNotifyOuterClass.SceneAreaWeatherNotify;

public final class SendSceneAreaWeatherNotify implements SendPacket {
    private final byte[] data;

    public SendSceneAreaWeatherNotify(Player player) {
        var proto =
            SceneAreaWeatherNotify.newBuilder()
                .setClimateType(player.getClimateType().getValue())
                .setWeatherAreaId(player.getWeatherId())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneAreaWeatherNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}