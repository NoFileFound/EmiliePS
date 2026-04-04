package org.genshinimpact.gameserver.packets.send.team;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.SyncTeamEntityNotifyOuterClass.SyncTeamEntityNotify;

public final class SendSyncTeamEntityNotify implements SendPacket {
    private final byte[] data;

    public SendSyncTeamEntityNotify(Player player) {
        var proto =
            SyncTeamEntityNotify.newBuilder()
                .setSceneId(player.getSceneId());

        if(player.getWorld().isMultiplayer()) {
            for(var playerEntry : player.getWorld().getPlayers()) {
                if(playerEntry != player) {
                    proto.addTeamEntityInfoList(
                        SyncTeamEntityNotify.TeamEntityInfo.newBuilder()
                            .setTeamEntityId(playerEntry.getAccount().getPlayerTeam().getEntity().getEntityId())
                            .setAuthorityPeerId(playerEntry.getPeerId())
                            .setTeamAbilityInfo(AbilitySyncStateInfo.newBuilder())
                            .build());
                }
            }
        }

        this.data = proto.build().toByteArray();
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