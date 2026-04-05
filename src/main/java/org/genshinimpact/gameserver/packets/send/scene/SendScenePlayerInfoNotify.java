package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.OnlinePlayerInfoOuterClass.OnlinePlayerInfo;
import org.generated.protobuf.ProfilePictureOuterClass.ProfilePicture;
import org.generated.protobuf.ScenePlayerInfoNotifyOuterClass.ScenePlayerInfoNotify;

public final class SendScenePlayerInfoNotify implements SendPacket {
    private final byte[] data;

    public SendScenePlayerInfoNotify(World world) {
        var proto = ScenePlayerInfoNotify.newBuilder();
        for(var playerEntry : world.getPlayers()) {
            proto.addPlayerInfoList(
                ScenePlayerInfoNotify.ScenePlayerInfo.newBuilder()
                    .setName(playerEntry.getAccount().getUsername())
                    .setOnlinePlayerInfo(
                        OnlinePlayerInfo.newBuilder()
                            .setCurPlayerNumInWorld(world.getPlayers().size())
                            .setMpSettingType(OnlinePlayerInfo.MpSettingType.MP_SETTING_ENTER_FREELY) ///  TODO: FIX
                            .setNameCardId(playerEntry.getAccount().getNameCardId())
                            .setNickname(playerEntry.getAccount().getUsername())
                            .setPlayerLevel(playerEntry.getAccount().getPlayerLevel())
                            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(playerEntry.getAccount().getProfileAvatarImageId()).setCostumeId(0).build()) ///  TODO: FIX
                            .setSignature(playerEntry.getAccount().getProfileSignature())
                            .setUid(playerEntry.getAccount().getId().intValue())
                            .build())
                    .setPeerId(playerEntry.getPeerId())
                    .setSceneId(playerEntry.getScene().getSceneId())
                    .setUid(playerEntry.getAccount().getId().intValue()).build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ScenePlayerInfoNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}