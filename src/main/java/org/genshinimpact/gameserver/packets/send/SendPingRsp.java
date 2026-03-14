package org.genshinimpact.gameserver.packets.send;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PingRspOuterClass.PingRsp;

public final class SendPingRsp implements SendPacket {
    private final byte[] data;

    public SendPingRsp(int clientTime, int clientSequenceId) {
        var proto =
            PingRsp.newBuilder()
                .setRetcode(RET_SUCC.getValue())
                .setClientTime(clientTime)
                .setSeq(clientSequenceId)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PingRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}