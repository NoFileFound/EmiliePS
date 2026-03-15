package org.genshinimpact.gameserver.packets.send.world;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PropValueOuterClass.PropValue;
import org.generated.protobuf.WorldDataNotifyOuterClass.WorldDataNotify;

public final class SendWorldDataNotify implements SendPacket {
    private final byte[] data;

    public SendWorldDataNotify(int worldLevel, boolean isMultiplayer) {
        var proto =
            WorldDataNotify.newBuilder()
                .putWorldPropMap(1, PropValue.newBuilder().setType(1).setIval(worldLevel).setVal(worldLevel).build())
                .putWorldPropMap(2, PropValue.newBuilder().setType(2).setIval(isMultiplayer ? 1 : 0).setVal(isMultiplayer ? 1 : 0).build())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.WorldDataNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}