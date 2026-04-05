package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendGetPlayerSocialDetailRsp;

// Protocol buffers
import org.generated.protobuf.GetPlayerSocialDetailReqOuterClass.GetPlayerSocialDetailReq;

public final class RecvGetPlayerSocialDetailReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var targetPlayer = server.getPlayers().get((long)GetPlayerSocialDetailReq.parseFrom(data).getUid());
        if(targetPlayer == null) {
            return;
        }

        ///  TODO: Add isFriend
        player.sendPacket(new SendGetPlayerSocialDetailRsp(targetPlayer.getPlayerSocialDetail()));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetPlayerSocialDetailReq;
    }
}