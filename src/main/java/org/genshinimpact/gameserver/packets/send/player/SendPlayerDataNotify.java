package org.genshinimpact.gameserver.packets.send.player;

// Imports
import java.util.Map;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerDataNotifyOuterClass.PlayerDataNotify;
import org.generated.protobuf.PropValueOuterClass.PropValue;

public final class SendPlayerDataNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerDataNotify(String nickname, boolean isFirstLogin, Map<Integer, Integer> playerProperties) {
        var proto =
            PlayerDataNotify.newBuilder()
                .setIsFirstLoginToday(isFirstLogin)
                .setNickName(nickname)
                .setRegionId(1)
                .setServerTime(System.currentTimeMillis());

        for(var propertyEntry : playerProperties.entrySet()) {
            int key = propertyEntry.getKey();
            int value = propertyEntry.getValue();
            proto.putPropMap(key, PropValue.newBuilder().setType(key).setIval(value).setVal(value).build());
        }

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerDataNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}