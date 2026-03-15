package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilityControlBlockOuterClass.AbilityControlBlock;
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.SceneEntityInfoOuterClass.SceneEntityInfo;
import org.generated.protobuf.SceneTeamUpdateNotifyOuterClass.SceneTeamUpdateNotify;

public class SendSceneTeamUpdateNotify implements SendPacket {
    private final byte[] data;

    public SendSceneTeamUpdateNotify(Player player) {
        var proto = SceneTeamUpdateNotify.newBuilder().setIsInMp(player.getWorld().getPlayers().size() > 1);
        for(var p : player.getWorld().getPlayers().values()) {
            for(var entityAvatar : p.getPlayerIdentity().getTeamList().get(p.getPlayerIdentity().getCurrentTeamId()).getAvatars()) {
                var avatarObj = p.getPlayerIdentity().getAvatars().get(entityAvatar);
                var avatarProto =
                        SceneTeamUpdateNotify.SceneTeamAvatar.newBuilder()
                                .setPlayerUid(p.getPlayerIdentity().getId().intValue())
                                .setAvatarGuid(avatarObj.getGuid())
                                .setSceneId(p.getScene().getSceneId())
                                .setEntityId(avatarObj.getAvatarId())
                                .setSceneEntityInfo(SceneEntityInfo.newBuilder().build())
                                .setWeaponGuid(0)
                                .setWeaponEntityId(0)
                                .setIsPlayerCurAvatar(true) // p.getTeamManager().getCurrentAvatarEntity() == entityAvatar
                                .setIsOnScene(true) // p.getTeamManager().getCurrentAvatarEntity() == entityAvatar
                                .setAvatarAbilityInfo(AbilitySyncStateInfo.newBuilder())
                                .setWeaponAbilityInfo(AbilitySyncStateInfo.newBuilder())
                                .setAbilityControlBlock(AbilityControlBlock.newBuilder());

                proto.addSceneTeamAvatarList(avatarProto);
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

/// TODO: FINISH