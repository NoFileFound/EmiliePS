package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_BIRTHDAY_CANNOT_BE_SET_TWICE;
import static org.genshinimpact.gameserver.enums.Retcode.RET_BIRTHDAY_FORMAT_ERROR;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendSetPlayerBirthdayRsp;

// Protocol buffers
import org.generated.protobuf.SetPlayerBirthdayReqOuterClass.SetPlayerBirthdayReq;

public final class RecvSetPlayerBirthdayReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = SetPlayerBirthdayReq.parseFrom(data);
        if(player.getAccount().getPlayerBirthday().isAlreadySet()) {
            player.sendPacket(new SendSetPlayerBirthdayRsp(RET_BIRTHDAY_CANNOT_BE_SET_TWICE));
            return;
        }

        int month = req.getBirthday().getMonth();
        int day = req.getBirthday().getDay();
        if(!isValidBirthday(day, month)) {
            player.sendPacket(new SendSetPlayerBirthdayRsp(RET_BIRTHDAY_FORMAT_ERROR));
            return;
        }

        player.getAccount().getPlayerBirthday().setBirthday(day, month);
        player.getAccount().save(true);
        player.sendPacket(new SendSetPlayerBirthdayRsp(player.getAccount().getPlayerBirthday()));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerBirthdayReq;
    }

    /**
     * Checks if the day and month are valid for a birthday.
     * @param day The day to check.
     * @param month The month to check.
     * @return True if its valid or else False.
     */
    public boolean isValidBirthday(int day, int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> day > 0 & day <= 31;
            case 4, 6, 9, 11 -> day > 0 && day <= 30;
            case 2 -> day > 0 & day <= 29;
            default -> false;
        };
    }
}