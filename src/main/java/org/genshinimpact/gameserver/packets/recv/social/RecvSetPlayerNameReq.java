package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendSetPlayerNameRsp;

// Protocol buffers
import org.generated.protobuf.SetPlayerNameReqOuterClass.SetPlayerNameReq;

public final class RecvSetPlayerNameReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var nickname = SetPlayerNameReq.parseFrom(data).getNickName();
        if(player.getAccount().isGuest()) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_FORBIDDEN));
            return;
        }

        if(nickname.isEmpty()) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_NICKNAME_IS_EMPTY));
            return;
        }

        if(!nickname.matches("[A-Za-z0-9]+")) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_NICKNAME_UTF_8_ERROR));
            return;
        }

        if(nickname.length() > 14) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_NICKNAME_TOO_LONG));
            return;
        }

        if(nickname.chars().filter(Character::isDigit).count() > 6) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_NICKNAME_TOO_MANY_DIGITS));
            return;
        }

        if(AppBootstrap.getMainConfig().badWords.stream().anyMatch(nickname.toLowerCase().replaceAll("[^a-z0-9]", "")::contains)) {
            player.sendPacket(new SendSetPlayerNameRsp(Retcode.RET_NICKNAME_WORD_ILLEGAL));
            return;
        }

        player.getAccount().setUsername(nickname);
        player.sendPacket(new SendSetPlayerNameRsp(nickname));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerNameReq;
    }
}