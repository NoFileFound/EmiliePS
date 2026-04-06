package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_NAME_CARD_NOT_UNLOCKED;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashSet;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendUpdatePlayerShowNameCardListRsp;

// Protocol buffers
import org.generated.protobuf.UpdatePlayerShowNameCardListReqOuterClass.UpdatePlayerShowNameCardListReq;

public final class RecvUpdatePlayerShowNameCardListReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var nameCardList = UpdatePlayerShowNameCardListReq.parseFrom(data).getShowNameCardIdListList();
        if(!player.getAccount().getUnlockedNameCards().containsAll(nameCardList)) {
            player.sendPacket(new SendUpdatePlayerShowNameCardListRsp(RET_NAME_CARD_NOT_UNLOCKED));
            return;
        }


        player.getAccount().setUnlockedNameCardsProfileShown(new HashSet<>(nameCardList));
        player.sendPacket(new SendUpdatePlayerShowNameCardListRsp(nameCardList));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.UpdatePlayerShowNameCardListReq;
    }
}