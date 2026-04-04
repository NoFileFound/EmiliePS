package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.EnterSceneDoneRspOuterClass.EnterSceneDoneRsp;

public final class SendEnterSceneDoneRsp implements SendPacket {
    private final byte[] data;

    public SendEnterSceneDoneRsp(Retcode retcode, int enterSceneToken) {
        var proto =
            EnterSceneDoneRsp.newBuilder()
                .setEnterSceneToken(enterSceneToken)
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.EnterSceneDoneRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}