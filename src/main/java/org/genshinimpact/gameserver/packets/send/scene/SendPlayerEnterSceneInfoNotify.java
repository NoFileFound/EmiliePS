package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AbilityControlBlockOuterClass.AbilityControlBlock;
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.PlayerEnterSceneInfoNotifyOuterClass.PlayerEnterSceneInfoNotify;

public class SendPlayerEnterSceneInfoNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerEnterSceneInfoNotify(Player player, int enterSceneToken) {
        var proto =
            PlayerEnterSceneInfoNotify.newBuilder()
                .setCurAvatarEntityId(player.getPlayerIdentity().getTeamList().get(0).getAvatars().get(0))
                .setEnterSceneToken(enterSceneToken)
                .setTeamEnterInfo(
                    PlayerEnterSceneInfoNotify.TeamEnterSceneInfo.newBuilder()
                        .setTeamEntityId(150995833)
                        .setAbilityControlBlock(AbilityControlBlock.newBuilder().build())
                        .setTeamAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                    .build())
                .setMpLevelEntityInfo(
                    PlayerEnterSceneInfoNotify.MPLevelEntityInfo.newBuilder()
                        .setAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                        .setEntityId(184550274)
                        .setAuthorityPeerId(player.getWorld().getWorldHost().getPeerId())
                    .build());

        for(var avatar : player.getPlayerIdentity().getTeamList().get(player.getPlayerIdentity().getCurrentTeamId()).getAvatars()) {
            var avatarObj = player.getPlayerIdentity().getAvatars().get(avatar);
            PlayerEnterSceneInfoNotify.AvatarEnterSceneInfo avatarInfo =
                    PlayerEnterSceneInfoNotify.AvatarEnterSceneInfo.newBuilder()
                            .setAvatarGuid(avatarObj.getGuid())
                            .setAvatarEntityId(avatarObj.getAvatarId())
                            .setWeaponGuid(0)
                            .setWeaponEntityId(0)
                            .setAvatarAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                            .setWeaponAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                            .build();

            proto.addAvatarEnterInfo(avatarInfo);
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

/// TODO: FINISH