package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerTimeNotifyOuterClass.PlayerTimeNotify;

public final class SendPlayerTimeNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerTimeNotify(Player player) {
        var proto =
            PlayerTimeNotify.newBuilder()
                .setIsPaused(player.isPaused())
                .setPlayerTime(player.getClientTime())
                .setServerTime(System.currentTimeMillis())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerTimeNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}