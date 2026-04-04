package org.genshinimpact.gameserver.packets.send.avatar;

// Imports
import java.util.stream.StreamSupport;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AvatarDataNotifyOuterClass.AvatarDataNotify;

public final class SendAvatarDataNotify implements SendPacket {
    private final byte[] data;

    public SendAvatarDataNotify(Player player) {
        var proto =
            AvatarDataNotify.newBuilder()
                .addAllAvatarList(() -> StreamSupport.stream(player.getAvatarStorage().spliterator(), false).map(Avatar::toProto).iterator())
                .addAllOwnedFlycloakList(player.getAccount().getOwnedFlyCloakList())
                .addAllOwnedCostumeList(player.getAccount().getOwnedCostumeList())
                .setCurAvatarTeamId(player.getAccount().getPlayerTeam().getCurrentTeamIndex())
                .setChooseAvatarGuid(player.getAccount().getPlayerTeam().getCurrentAvatarEntity().getAvatar().getAvatarGuid());

        for(var teamEntry : player.getAccount().getPlayerTeam().getTeams().entrySet()) {
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