package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ProfilePictureOuterClass.ProfilePicture;
import org.generated.protobuf.SetPlayerHeadImageRspOuterClass.SetPlayerHeadImageRsp;

public final class SendSetPlayerHeadImageRsp implements SendPacket {
    private final byte[] data;

    public SendSetPlayerHeadImageRsp(Retcode retcode) {
        var proto =
            SetPlayerHeadImageRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSetPlayerHeadImageRsp(int avatarId, int costumeId) {
        var proto =
            SetPlayerHeadImageRsp.newBuilder()
                .setAvatarId(avatarId)
                .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(avatarId).setCostumeId(costumeId).build())
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return PacketIdentifiers.Send.SetPlayerHeadImageRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}