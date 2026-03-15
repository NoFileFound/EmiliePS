package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SceneInitFinishRspOuterClass.SceneInitFinishRsp;

public final class SendSceneInitFinishRsp implements SendPacket {
    private final byte[] data;

    public SendSceneInitFinishRsp(Retcode retcode, int enterSceneToken) {
        var proto =
            SceneInitFinishRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .setEnterSceneToken(enterSceneToken)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SceneInitFinishRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}