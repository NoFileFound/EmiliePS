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
                    .setMpSettingType(OnlinePlayerInfo.MpSettingType.MP_SETTING_NO_ENTER) ///  TODO: FIX
                    .setNameCardId(0) ///  TODO: FIX
                    .setNickname(playerEntry.getAccount().getUsername())
                    .setPlayerLevel(0) ///  TODO: FIX
                    .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(playerEntry.getAccount().getProfileAvatarImageId()).build()) ///  TODO: FIX
                    .setSignature("EmiliePS") ///  TODO: FIX
                    .setUid(playerEntry.getAccount().getId().intValue())
                    .build());

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