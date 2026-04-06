package org.genshinimpact.gameserver.packets.send.social;

// Imports
import java.util.List;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.UpdatePlayerShowAvatarListRspOuterClass.UpdatePlayerShowAvatarListRsp;

public final class SendUpdatePlayerShowAvatarListRsp implements SendPacket {
    private final byte[] data;

    public SendUpdatePlayerShowAvatarListRsp(Retcode retcode) {
        var proto =
            UpdatePlayerShowAvatarListRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendUpdatePlayerShowAvatarListRsp(List<Integer> avatarList, boolean showAvatars) {
        var proto =
            UpdatePlayerShowAvatarListRsp.newBuilder()
                .addAllShowAvatarIdList(avatarList)
                .setIsShowAvatar(showAvatars)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.UpdatePlayerShowAvatarListRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}