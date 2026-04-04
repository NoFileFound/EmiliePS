package org.genshinimpact.gameserver.packets.send;

// Imports
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ServerTimeNotifyOuterClass.ServerTimeNotify;

public final class SendServerTimeNotify implements SendPacket {
    private final byte[] data;

    public SendServerTimeNotify() {
        var proto =
            ServerTimeNotify.newBuilder()
                .setServerTime(System.currentTimeMillis())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return PacketIdentifiers.Send.ServerTimeNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}