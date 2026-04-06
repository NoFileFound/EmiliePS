package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SetPlayerNameRspOuterClass.SetPlayerNameRsp;

public final class SendSetPlayerNameRsp implements SendPacket {
    private final byte[] data;

    public SendSetPlayerNameRsp(Retcode retcode) {
        var proto =
            SetPlayerNameRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSetPlayerNameRsp(String nickname) {
        var proto =
            SetPlayerNameRsp.newBuilder()
                .setNickName(nickname)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return PacketIdentifiers.Send.SetPlayerNameRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}