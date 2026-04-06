package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_PSN_GET_PLAYER_SOCIAL_DETAIL_FAIL;
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
        long targetUid = GetPlayerSocialDetailReq.parseFrom(data).getUid();
        var targetSocialInfo = server.getPlayerSocialInfo(targetUid, player.getAccount().getFriendsList().contains(targetUid), player.getAccount().getIgnoredList().contains(targetUid));
        if(targetSocialInfo == null) {
            player.sendPacket(new SendGetPlayerSocialDetailRsp(RET_PSN_GET_PLAYER_SOCIAL_DETAIL_FAIL));
            return;
        }

        player.sendPacket(new SendGetPlayerSocialDetailRsp(targetSocialInfo));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetPlayerSocialDetailReq;
    }
}