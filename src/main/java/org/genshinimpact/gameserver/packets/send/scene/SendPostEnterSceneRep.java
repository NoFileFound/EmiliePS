package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PostEnterSceneRspOuterClass.PostEnterSceneRsp;

public final class SendPostEnterSceneRep implements SendPacket {
    private final byte[] data;

    public SendPostEnterSceneRep(Retcode retcode, int enterSceneToken) {
        var proto =
            PostEnterSceneRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .setEnterSceneToken(enterSceneToken)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PostEnterSceneRep;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}