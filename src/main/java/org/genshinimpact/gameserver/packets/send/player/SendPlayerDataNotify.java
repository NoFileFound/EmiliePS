package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerDataNotifyOuterClass.PlayerDataNotify;

public final class SendPlayerDataNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerDataNotify(String nickname, boolean isFirstLogin) {
        var proto =
            PlayerDataNotify
                .newBuilder()
                .setNickName(nickname)
                .setServerTime(System.currentTimeMillis())
                .setRegionId(1)
                .setIsFirstLoginToday(isFirstLogin)
                .build();

        ///  TODO: Properties.

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerDataNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}