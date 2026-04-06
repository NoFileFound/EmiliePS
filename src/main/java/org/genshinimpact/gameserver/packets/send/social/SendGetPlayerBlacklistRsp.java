package org.genshinimpact.gameserver.packets.send.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import java.util.List;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.FriendBriefOuterClass.FriendBrief;
import org.generated.protobuf.GetPlayerBlacklistRspOuterClass.GetPlayerBlacklistRsp;

public final class SendGetPlayerBlacklistRsp implements SendPacket {
    private final byte[] data;

    public SendGetPlayerBlacklistRsp(List<FriendBrief> ignoredList) {
        var proto =
            GetPlayerBlacklistRsp.newBuilder()
                .addAllBlacklist(ignoredList)
                .setRetcode(RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.GetPlayerBlacklistRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}