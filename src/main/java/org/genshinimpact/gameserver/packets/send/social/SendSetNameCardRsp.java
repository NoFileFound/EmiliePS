package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SetNameCardRspOuterClass.SetNameCardRsp;

public final class SendSetNameCardRsp implements SendPacket {
    private final byte[] data;

    public SendSetNameCardRsp(Retcode retcode) {
        var proto =
            SetNameCardRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSetNameCardRsp(int nameCardId) {
        var proto =
            SetNameCardRsp.newBuilder()
                .setNameCardId(nameCardId)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SetNameCardRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}