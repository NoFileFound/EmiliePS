package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendGetPlayerBlacklistRsp;

// Protocol buffers
import org.generated.protobuf.FriendBriefOuterClass.FriendBrief;

public final class RecvGetPlayerBlacklistReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        List<FriendBrief> ignoredList = new ArrayList<>();
        for(var ignoredUid : player.getAccount().getIgnoredList()) {
            ignoredList.add(server.getPlayerBriefInfo(ignoredUid));
        }

        player.sendPacket(new SendGetPlayerBlacklistRsp(ignoredList));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetPlayerBlacklistReq;
    }
}