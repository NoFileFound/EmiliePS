package org.genshinimpact.gameserver.packets.recv.chat;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.chat.SendGetChatEmojiCollectionRsp;

public final class RecvGetChatEmojiCollectionReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        player.sendPacket(new SendGetChatEmojiCollectionRsp(player.getAccount().getChatEmojiCollection()));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetChatEmojiCollectionReq;
    }
}