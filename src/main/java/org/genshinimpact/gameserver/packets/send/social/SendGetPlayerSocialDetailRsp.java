package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.GetPlayerSocialDetailRspOuterClass.GetPlayerSocialDetailRsp;
import org.generated.protobuf.SocialDetailOuterClass.SocialDetail;

public final class SendGetPlayerSocialDetailRsp implements SendPacket {
    private final byte[] data;

    public SendGetPlayerSocialDetailRsp(Retcode retcode) {
        var proto =
            GetPlayerSocialDetailRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendGetPlayerSocialDetailRsp(SocialDetail socialDetail) {
        var proto =
            GetPlayerSocialDetailRsp.newBuilder()
                .setDetailData(socialDetail)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.GetPlayerSocialDetailRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}