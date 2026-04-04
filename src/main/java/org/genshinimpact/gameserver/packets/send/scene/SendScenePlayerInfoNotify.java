package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.OnlinePlayerInfoOuterClass.OnlinePlayerInfo;
import org.generated.protobuf.ProfilePictureOuterClass.ProfilePicture;
import org.generated.protobuf.ScenePlayerInfoNotifyOuterClass.ScenePlayerInfoNotify;

public class SendScenePlayerInfoNotify implements SendPacket {
    private final byte[] data;

    public SendScenePlayerInfoNotify(World world) {
        var proto = ScenePlayerInfoNotify.newBuilder();
        for(var playerEntry : world.getPlayers()) {
            proto.addPlayerInfoList(
                ScenePlayerInfoNotify.ScenePlayerInfo.newBuilder()
                    .setName(playerEntry.getAccount().getUsername())
                    .setOnlinePlayerInfo(
                        OnlinePlayerInfo.newBuilder()
                            .setMpSettingType(OnlinePlayerInfo.MpSettingType.MP_SETTING_NO_ENTER) ///  TODO: FIX
                            .setNameCardId(0) ///  TODO: FIX
                            .setNickname(playerEntry.getAccount().getUsername())
                            .setPlayerLevel(0) ///  TODO: FIX
                            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(playerEntry.getAccount().getProfileAvatarImageId()).build()) ///  TODO: FIX
                            .setSignature("EmiliePS") ///  TODO: FIX
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

/// TODO: FINISH