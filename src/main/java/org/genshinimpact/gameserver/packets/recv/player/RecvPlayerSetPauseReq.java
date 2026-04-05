package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_FAIL;
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.player.SendPlayerSetPauseRsp;

// Protocol buffers
import org.generated.protobuf.PlayerSetPauseReqOuterClass.PlayerSetPauseReq;

public final class RecvPlayerSetPauseReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        if(player.getWorld().isMultiplayer()) {
            player.sendPacket(new SendPlayerSetPauseRsp(RET_FAIL));
        } else {
            player.getWorld().setPaused(PlayerSetPauseReq.parseFrom(data).getIsPaused());
            player.sendPacket(new SendPlayerSetPauseRsp(RET_SUCC));
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PlayerSetPauseReq;
    }
}