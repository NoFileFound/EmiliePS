package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.PlayerEnterSceneInfoNotifyOuterClass.PlayerEnterSceneInfoNotify;

public final class SendPlayerEnterSceneInfoNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerEnterSceneInfoNotify(Player player, int enterSceneToken) {
        var proto =
            PlayerEnterSceneInfoNotify.newBuilder()
                .setCurAvatarEntityId(player.getAccount().getPlayerTeam().getCurrentAvatarEntity().getEntityId())
                .setEnterSceneToken(enterSceneToken)
                .setMpLevelEntityInfo(
                    PlayerEnterSceneInfoNotify.MPLevelEntityInfo.newBuilder()
                        .setAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                        .setAuthorityPeerId(player.getWorld().getWorldHost().getPeerId())
                        .setEntityId(player.getWorld().getEntity().getEntityId()))
                .setTeamEnterInfo(
                    PlayerEnterSceneInfoNotify.TeamEnterSceneInfo.newBuilder()
                        .setAbilityControlBlock(player.getAccount().getPlayerTeam().getAbilityControlBlock())
                        .setTeamAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                        .setTeamEntityId(player.getAccount().getPlayerTeam().getEntity().getEntityId()));

        for(var avatarEntityEntry : player.getAccount().getPlayerTeam().getEntityAvatarList()) {
            proto.addAvatarEnterInfo(
                PlayerEnterSceneInfoNotify.AvatarEnterSceneInfo.newBuilder()
                    .setAvatarAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .setAvatarEntityId(avatarEntityEntry.getEntityId())
                    .setAvatarGuid(avatarEntityEntry.getAvatar().getAvatarGuid())
                    .setWeaponAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .setWeaponEntityId(avatarEntityEntry.getAvatar().getWeapon() != null ? avatarEntityEntry.getAvatar().getWeapon().getItemEntity().getEntityId() : 0)
                    .setWeaponGuid(avatarEntityEntry.getAvatar().getWeapon() != null ? avatarEntityEntry.getAvatar().getWeapon().getItemGuid() : 0)
                    .build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerEnterSceneInfoNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}