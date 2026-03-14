package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ServerDisconnectClientNotifyOuterClass.ServerDisconnectClientNotify;

public final class SendServerDisconnectClientNotify implements SendPacket {
    private final byte[] data;

    public SendServerDisconnectClientNotify() {
        var proto =
            ServerDisconnectClientNotify.newBuilder()
                .setData(1)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ServerDisconnectClientNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}