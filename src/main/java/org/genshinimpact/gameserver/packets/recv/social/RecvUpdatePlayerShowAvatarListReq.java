package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashSet;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendUpdatePlayerShowAvatarListRsp;

// Protocol buffers
import org.generated.protobuf.UpdatePlayerShowAvatarListReqOuterClass.UpdatePlayerShowAvatarListReq;

public final class RecvUpdatePlayerShowAvatarListReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = UpdatePlayerShowAvatarListReq.parseFrom(data);
        var avatarList = req.getShowAvatarIdListList();
        var showAvatars = req.getIsShowAvatar();
        for(var avatarEntry : avatarList) {
            if(!player.getAvatarStorage().hasAvatar(avatarEntry)) {
                return;
            }
        }

        player.getAccount().setUnlockedAvatarsProfileShown(new HashSet<>(avatarList));
        player.getAccount().setShowProfileAvatars(showAvatars);
        player.sendPacket(new SendUpdatePlayerShowAvatarListRsp(avatarList, showAvatars));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.UpdatePlayerShowAvatarListReq;
    }
}