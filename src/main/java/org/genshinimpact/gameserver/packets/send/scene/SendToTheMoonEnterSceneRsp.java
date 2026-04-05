package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ToTheMoonEnterSceneRspOuterClass.ToTheMoonEnterSceneRsp;

public final class SendToTheMoonEnterSceneRsp implements SendPacket {
    private final byte[] data;

    public SendToTheMoonEnterSceneRsp(Retcode retcode) {
        var proto =
            ToTheMoonEnterSceneRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ToTheMoonEnterSceneRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}