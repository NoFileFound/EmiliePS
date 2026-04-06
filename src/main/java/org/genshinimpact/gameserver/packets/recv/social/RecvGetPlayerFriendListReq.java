package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendGetPlayerFriendListRsp;

// Protocol buffers
import org.generated.protobuf.FriendBriefOuterClass.FriendBrief;

public final class RecvGetPlayerFriendListReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        List<FriendBrief> friendBriefList = new ArrayList<>();
        for(var friendUid : player.getAccount().getFriendsList()) {
            friendBriefList.add(server.getPlayerBriefInfo(friendUid));
        }

        List<FriendBrief> askFriendBriefList = new ArrayList<>();
        for(var askFriendUid : player.getAccount().getAskFriendsList()) {
            askFriendBriefList.add(server.getPlayerBriefInfo(askFriendUid));
        }

        player.sendPacket(new SendGetPlayerFriendListRsp(server.getPlayerBriefInfo(0), friendBriefList, askFriendBriefList));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetPlayerFriendListReq;
    }
}