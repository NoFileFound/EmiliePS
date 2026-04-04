package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerGameTimeNotifyOuterClass.PlayerGameTimeNotify;

public final class SendPlayerGameTimeNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerGameTimeNotify(long playerId, long playerGameTime) {
        var proto =
            PlayerGameTimeNotify.newBuilder()
                .setGameTime((int)(playerGameTime / 1000))
                .setUid((int)playerId)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerGameTimeNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}