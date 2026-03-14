package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.player.SendSetPlayerBornDataRsp;

// Protocol buffers
import org.generated.protobuf.SetPlayerBornDataReqOuterClass.SetPlayerBornDataReq;

public final class RecvSetPlayerBornDataReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = SetPlayerBornDataReq.parseFrom(data);
        var myPlayer = session.getPlayer();
        if(myPlayer == null || !myPlayer.getPlayerIdentity().getAvatars().isEmpty()) {
            session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_FAIL));
            return;
        }

        var avatarId = req.getAvatarId();
        if(avatarId == 10000005 || avatarId == 10000007)
        {
            var nickname = req.getNickName();
            if(nickname.isEmpty()) {
                session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_IS_EMPTY));
                return;
            }

            if(!nickname.matches("[A-Za-z0-9]+")) {
                session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_UTF_8_ERROR));
                return;
            }

            if(nickname.length() > 14) {
                session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_TOO_LONG));
                return;
            }

            if(nickname.chars().filter(Character::isDigit).count() > 6) {
                session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_TOO_MANY_DIGITS));
                return;
            }

            if(AppBootstrap.getMainConfig().badWords.stream().anyMatch(nickname.toLowerCase().replaceAll("[^a-z0-9]", "")::contains)) {
                session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_NICKNAME_WORD_ILLEGAL));
                return;
            }

            myPlayer.getPlayerIdentity().getAvatars().put(avatarId, new Avatar(avatarId));
            myPlayer.getPlayerIdentity().setCurrentAvatarId(avatarId);
            myPlayer.getPlayerIdentity().setUsername(nickname);
            myPlayer.getPlayerIdentity().save(true);
            myPlayer.sendLogin();
            session.sendPacket(new SendSetPlayerBornDataRsp(Retcode.RET_SUCC));
        } else {
            session.getTunnel().close();
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerBornDataReq;
    }
}