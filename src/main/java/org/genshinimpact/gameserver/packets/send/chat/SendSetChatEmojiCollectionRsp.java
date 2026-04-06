package org.genshinimpact.gameserver.packets.send.chat;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.SetChatEmojiCollectionRspOuterClass.SetChatEmojiCollectionRsp;

public final class SendSetChatEmojiCollectionRsp implements SendPacket {
    private final byte[] data;

    public SendSetChatEmojiCollectionRsp() {
        this.data = SetChatEmojiCollectionRsp.newBuilder().setRetcode(RET_SUCC.getValue()).build().toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SetChatEmojiCollectionRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}