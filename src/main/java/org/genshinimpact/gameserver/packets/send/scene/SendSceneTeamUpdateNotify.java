package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.SceneTeamUpdateNotifyOuterClass.SceneTeamUpdateNotify;

public final class SendSceneTeamUpdateNotify implements SendPacket {
    private final byte[] data;

    public SendSceneTeamUpdateNotify(Player player) {
        boolean isMultiplayer = player.getWorld().isMultiplayer();
        var proto = SceneTeamUpdateNotify.newBuilder().setIsInMp(isMultiplayer);
        for(var playerEntry : player.getWorld().getPlayers()) {
            for(var entityAvatarEntry : playerEntry.getAccount().getPlayerTeam().getEntityAvatarList()) {
                var sceneAvatar = SceneTeamUpdateNotify.SceneTeamAvatar.newBuilder()
                    .setAbilityControlBlock(entityAvatarEntry.getAbilityControlBlock())
                    .setAvatarAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .setAvatarGuid(entityAvatarEntry.getAvatar().getAvatarGuid())
                    .setEntityId(entityAvatarEntry.getEntityId())
                    .setIsOnScene(playerEntry.getAccount().getPlayerTeam().getCurrentAvatarEntity() == entityAvatarEntry)
                    .setIsPlayerCurAvatar(playerEntry.getAccount().getPlayerTeam().getCurrentAvatarEntity() == entityAvatarEntry)
                    .setIsReconnect(true)
                    .setPlayerUid(playerEntry.getAccount().getId().intValue())
                    .setSceneEntityInfo(entityAvatarEntry.toProto())
                    .setSceneId(playerEntry.getSceneId())
                    .setWeaponAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .setWeaponEntityId(entityAvatarEntry.getAvatar().getWeapon() != null ? entityAvatarEntry.getAvatar().getWeapon().getItemEntity().getEntityId() : 0)
                    .setWeaponGuid(entityAvatarEntry.getAvatar().getWeapon() != null ? entityAvatarEntry.getAvatar().getWeapon().getItemGuid() : 0);

                if(isMultiplayer) {
                    sceneAvatar.setAvatarInfo(entityAvatarEntry.getAvatar().toProto());
                    sceneAvatar.setSceneAvatarInfo(entityAvatarEntry.getSceneAvatarInfo());
                }

                proto.addSceneTeamAvatarList(sceneAvatar);
            }
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneTeamUpdateNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}