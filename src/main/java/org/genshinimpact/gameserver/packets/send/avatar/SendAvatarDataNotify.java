package org.genshinimpact.gameserver.packets.send.avatar;

// Imports
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AvatarDataNotifyOuterClass.AvatarDataNotify;

public final class SendAvatarDataNotify implements SendPacket {
    private final byte[] data;

    public SendAvatarDataNotify(Player player) {
        var currentTeamId = player.getPlayerIdentity().getCurrentTeamId();
        var proto =
            AvatarDataNotify.newBuilder()
                .setCurAvatarTeamId(currentTeamId)
                .setChooseAvatarGuid(player.getPlayerIdentity().getAvatars().get(player.getPlayerIdentity().getTeamList().get(currentTeamId).getAvatars().get(0)).getGuid())
                .addAllOwnedFlycloakList(player.getPlayerIdentity().getFlyCloakList())
                .addAllOwnedCostumeList(player.getPlayerIdentity().getCostumeList());

        proto.addAllTempAvatarGuidList(player.getTempAvatarGuidList());
        for(var avatarEntry : player.getPlayerIdentity().getAvatars().values()) {
            proto.addAvatarList(avatarEntry.toProto());
        }

        for(var teamEntry : player.getPlayerIdentity().getTeamList().entrySet()) {
            int id = teamEntry.getKey();
            proto.putAvatarTeamMap(id, teamEntry.getValue().toProto(player));
            if(id > 4) {
                proto.addBackupAvatarTeamOrderList(id);
            }
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.AvatarDataNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}