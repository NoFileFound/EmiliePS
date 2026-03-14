package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SetPlayerBornDataRspOuterClass.SetPlayerBornDataRsp;

public final class SendSetPlayerBornDataRsp implements SendPacket {
    private final byte[] data;

    public SendSetPlayerBornDataRsp(Retcode retcode) {
        var proto =
            SetPlayerBornDataRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SetPlayerBornDataRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}