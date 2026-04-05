package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SetPlayerSignatureRspOuterClass.SetPlayerSignatureRsp;

public final class SendSetPlayerSignatureRsp implements SendPacket {
    private final byte[] data;

    public SendSetPlayerSignatureRsp(Retcode retcode) {
        var proto =
            SetPlayerSignatureRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSetPlayerSignatureRsp(String signature) {
        var proto =
            SetPlayerSignatureRsp.newBuilder()
                .setRetcode(Retcode.RET_SUCC.getValue())
                .setSignature(signature)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SetPlayerSignatureRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}