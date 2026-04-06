package org.genshinimpact.gameserver.packets.send.chat;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import java.util.Set;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.GetChatEmojiCollectionRspOuterClass.GetChatEmojiCollectionRsp;

public final class SendGetChatEmojiCollectionRsp implements SendPacket {
    private final byte[] data;

    public SendGetChatEmojiCollectionRsp(Set<Integer> chatEmojiCollection) {
        var proto =
            GetChatEmojiCollectionRsp.newBuilder()
                .setChatEmojiCollectionData(GetChatEmojiCollectionRsp.ChatEmojiCollectionData.newBuilder().addAllEmojiIdList(chatEmojiCollection).build())
                .setRetcode(RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.GetChatEmojiCollectionRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}