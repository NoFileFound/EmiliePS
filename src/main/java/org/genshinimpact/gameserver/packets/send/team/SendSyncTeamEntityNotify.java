package org.genshinimpact.gameserver.packets.send.team;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SyncTeamEntityNotifyOuterClass.SyncTeamEntityNotify;

public class SendSyncTeamEntityNotify implements SendPacket {
    private final byte[] data;

    public SendSyncTeamEntityNotify(Player player) {
        var proto =
                SyncTeamEntityNotify.newBuilder()
                        .setSceneId(player.getScene().getSceneId())
                        .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SyncTeamEntityNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}

/// TODO: FINISH