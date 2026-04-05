package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.player.SendSetPlayerBornDataRsp;

// Protocol buffers
import org.generated.protobuf.SetPlayerBornDataReqOuterClass.SetPlayerBornDataReq;

public final class RecvSetPlayerBornDataReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = SetPlayerBornDataReq.parseFrom(data);
        if(!player.getAccount().getUnlockedAvatars().isEmpty() || player.getAccount().isGuest()) {
            player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_FORBIDDEN));
            return;
        }

        var avatarId = req.getAvatarId();
        if(avatarId == 10000005 || avatarId == 10000007) ///  TODO: Make better way to check if avatarId exist.
        {
            var nickname = req.getNickName();
            if(nickname.isEmpty()) {
                player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_IS_EMPTY));
                return;
            }

            if(!nickname.matches("[A-Za-z0-9]+")) {
                player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_UTF_8_ERROR));
                return;
            }

            if(nickname.length() > 14) {
                player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_TOO_LONG));
                return;
            }

            if(nickname.chars().filter(Character::isDigit).count() > 6) {
                player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_TOO_MANY_DIGITS));
                return;
            }

            if(AppBootstrap.getMainConfig().badWords.stream().anyMatch(nickname.toLowerCase().replaceAll("[^a-z0-9]", "")::contains)) {
                player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_WORD_ILLEGAL));
                return;
            }

            player.getAvatarStorage().addAvatar(avatarId, true);
            player.getAccount().setMainCharacterId(avatarId);
            player.getAccount().setProfileAvatarImageId(avatarId);
            player.getAccount().setUsername(nickname);
            player.getAccount().save(true);
            player.sendLogin();
            player.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_SUCC));
        } else {
            server.sendPlayerSanction(player.getAccount().getId(), 7 * 24, "System");
            player.closeConnection();
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerBornDataReq;
    }
}