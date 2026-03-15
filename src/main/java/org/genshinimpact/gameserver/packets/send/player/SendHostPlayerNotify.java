package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.HostPlayerNotifyOuterClass.HostPlayerNotify;

public final class SendHostPlayerNotify implements SendPacket {
    private final byte[] data;

    public SendHostPlayerNotify(long hostPlayerId, int hostPlayerPeerId) {
        var proto =
            HostPlayerNotify.newBuilder()
                .setHostPeerId(hostPlayerPeerId)
                .setHostUid((int)hostPlayerId)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.HostPlayerNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}