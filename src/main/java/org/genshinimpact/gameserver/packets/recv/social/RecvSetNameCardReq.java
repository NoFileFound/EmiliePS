package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_NAME_CARD_NOT_UNLOCKED;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Protocol buffers
import org.genshinimpact.gameserver.packets.send.social.SendSetNameCardRsp;

// Protocol buffers
import org.generated.protobuf.SetNameCardReqOuterClass.SetNameCardReq;

public final class RecvSetNameCardReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var nameCardId = SetNameCardReq.parseFrom(data).getNameCardId();
        if(!player.getAccount().getUnlockedNameCards().contains(nameCardId)) {
            player.sendPacket(new SendSetNameCardRsp(RET_NAME_CARD_NOT_UNLOCKED));
            return;
        }

        player.getAccount().setNameCardId(nameCardId);
        player.sendPacket(new SendSetNameCardRsp(nameCardId));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetNameCardReq;
    }
}