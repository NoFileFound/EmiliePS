package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerSetPauseRspOuterClass.PlayerSetPauseRsp;

public final class SendPlayerSetPauseRsp implements SendPacket {
    private final byte[] data;

    public SendPlayerSetPauseRsp(Retcode retcode) {
        var proto =
            PlayerSetPauseRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerSetPauseRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}