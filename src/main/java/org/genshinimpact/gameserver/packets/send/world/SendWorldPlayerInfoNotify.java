package org.genshinimpact.gameserver.packets.send.world;

// Imports
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ProfilePictureOuterClass.ProfilePicture;
import org.generated.protobuf.OnlinePlayerInfoOuterClass.OnlinePlayerInfo;
import org.generated.protobuf.WorldPlayerInfoNotifyOuterClass.WorldPlayerInfoNotify;

public final class SendWorldPlayerInfoNotify implements SendPacket {
    private final byte[] data;

    public SendWorldPlayerInfoNotify(World world) {
        var proto = WorldPlayerInfoNotify.newBuilder();
        for(var playerEntry : world.getPlayers()) {
            proto.addPlayerInfoList(
                OnlinePlayerInfo.newBuilder()
                    .setCurPlayerNumInWorld(world.getPlayers().size())
                    .setMpSettingType(OnlinePlayerInfo.MpSettingType.MP_SETTING_ENTER_FREELY) ///  TODO: FIX
                    .setNameCardId(playerEntry.getAccount().getNameCardId())
                    .setNickname(playerEntry.getAccount().getUsername())
                    .setPlayerLevel(playerEntry.getAccount().getPlayerLevel())
                    .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(playerEntry.getAccount().getProfileAvatarImageId()).setCostumeId(0).build()) ///  TODO: FIX
                    .setSignature(playerEntry.getAccount().getProfileSignature())
                    .setUid(playerEntry.getAccount().getId().intValue())
                    .build()
            );

            proto.addPlayerUidList(playerEntry.getAccount().getId().intValue());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.WorldPlayerInfoNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}