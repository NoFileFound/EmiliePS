package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.SyncScenePlayTeamEntityNotifyOuterClass.SyncScenePlayTeamEntityNotify;

public final class SendSyncScenePlayTeamEntityNotify implements SendPacket {
    private final byte[] data;

    public SendSyncScenePlayTeamEntityNotify(Player player) {
        var proto =
            SyncScenePlayTeamEntityNotify.newBuilder()
                .setSceneId(player.getSceneId());

        for(var entityEntry : player.getScene().getSceneEntities().values()) {
            proto.addEntityInfoList(
                SyncScenePlayTeamEntityNotify.PlayTeamEntityInfo.newBuilder()
                    .setAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .setAuthorityPeerId(player.getPeerId())
                    .setEntityId(entityEntry.getEntityId())
                    .setPlayerUid(player.getAccount().getId().intValue())
                    .build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SyncScenePlayTeamEntityNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}