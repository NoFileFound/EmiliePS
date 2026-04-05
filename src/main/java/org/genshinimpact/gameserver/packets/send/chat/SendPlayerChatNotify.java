package org.genshinimpact.gameserver.packets.send.chat;

// Imports
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ChatInfoOuterClass.ChatInfo;
import org.generated.protobuf.PlayerChatNotifyOuterClass.PlayerChatNotify;

public final class SendPlayerChatNotify implements SendPacket {
    private final byte[] data;

    public SendPlayerChatNotify(long senderId, int channelId, int systemHint) {
        var proto =
            PlayerChatNotify.newBuilder()
                .setChannelId(channelId)
                .setChatInfo(ChatInfo.newBuilder().setSystemHint(ChatInfo.SystemHint.newBuilder().setType(systemHint).build()).setTime((int)(System.currentTimeMillis() / 1000)).setUid((int)senderId).build())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerChatNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}