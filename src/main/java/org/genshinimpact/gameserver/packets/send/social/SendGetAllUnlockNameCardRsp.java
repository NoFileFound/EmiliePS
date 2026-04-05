package org.genshinimpact.gameserver.packets.send.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import java.util.Set;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.GetAllUnlockNameCardRspOuterClass.GetAllUnlockNameCardRsp;

public final class SendGetAllUnlockNameCardRsp implements SendPacket {
    private final byte[] data;

    public SendGetAllUnlockNameCardRsp(Set<Integer> unlockedNameCards) {
        var proto =
            GetAllUnlockNameCardRsp.newBuilder()
                .addAllNameCardList(unlockedNameCards)
                .setRetcode(RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.GetAllUnlockNameCardRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}