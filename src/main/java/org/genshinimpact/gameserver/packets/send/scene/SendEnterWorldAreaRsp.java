package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.EnterWorldAreaRspOuterClass.EnterWorldAreaRsp;

public final class SendEnterWorldAreaRsp implements SendPacket {
    private final byte[] data;

    public SendEnterWorldAreaRsp(Retcode retcode) {
        var proto =
            EnterWorldAreaRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendEnterWorldAreaRsp(int areaId, int areaType) {
        var proto =
            EnterWorldAreaRsp.newBuilder()
                .setAreaId(areaId)
                .setAreaType(areaType)
                .setRetcode(RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.EnterWorldAreaRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}