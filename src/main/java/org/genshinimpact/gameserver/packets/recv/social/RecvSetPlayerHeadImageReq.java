package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_PROFILE_PICTURE_NOT_UNLOCKED;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Protocol buffers
import org.generated.protobuf.SetPlayerHeadImageReqOuterClass.SetPlayerHeadImageReq;
import org.genshinimpact.gameserver.packets.send.social.SendSetPlayerHeadImageRsp;

public final class RecvSetPlayerHeadImageReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var headImageId = SetPlayerHeadImageReq.parseFrom(data).getAvatarId();
        if(!player.getAccount().getUnlockedAvatars().containsKey(headImageId)) {
            player.sendPacket(new SendSetPlayerHeadImageRsp(RET_PROFILE_PICTURE_NOT_UNLOCKED));
            return;
        }

        player.getAccount().setProfileAvatarImageId(headImageId);
        player.sendPacket(new SendSetPlayerHeadImageRsp(headImageId, player.getAccount().getProfileAvatarCostumeImageId()));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerHeadImageReq;
    }
}