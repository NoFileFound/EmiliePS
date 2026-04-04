package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.EnterSceneReadyRspOuterClass.EnterSceneReadyRsp;

public final class SendEnterSceneReadyRsp implements SendPacket {
    private final byte[] data;

    public SendEnterSceneReadyRsp(Retcode retcode, int enterSceneToken) {
        var proto =
            EnterSceneReadyRsp.newBuilder()
                .setEnterSceneToken(enterSceneToken)
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.EnterSceneReadyRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}