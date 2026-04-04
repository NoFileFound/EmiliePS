package org.genshinimpact.gameserver.packets.send.scene;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.EnterScenePeerNotifyOuterClass.EnterScenePeerNotify;

public final class SendEnterScenePeerNotify implements SendPacket {
    private final byte[] data;

    public SendEnterScenePeerNotify(int enterSceneToken, int destinationSceneId, int peerId, int hostPeerId) {
        var proto =
            EnterScenePeerNotify.newBuilder()
                .setEnterSceneToken(enterSceneToken)
                .setDestSceneId(destinationSceneId)
                .setHostPeerId(hostPeerId)
                .setPeerId(peerId)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.EnterScenePeerNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}