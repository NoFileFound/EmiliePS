package org.genshinimpact.gameserver.packets.send.social;

// Imports
import java.util.List;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.UpdatePlayerShowNameCardListRspOuterClass.UpdatePlayerShowNameCardListRsp;

public final class SendUpdatePlayerShowNameCardListRsp implements SendPacket {
    private final byte[] data;

    public SendUpdatePlayerShowNameCardListRsp(Retcode retcode) {
        var proto =
            UpdatePlayerShowNameCardListRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendUpdatePlayerShowNameCardListRsp(List<Integer> nameCardList) {
        var proto =
            UpdatePlayerShowNameCardListRsp.newBuilder()
                .addAllShowNameCardIdList(nameCardList)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return PacketIdentifiers.Send.UpdatePlayerShowNameCardListRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}