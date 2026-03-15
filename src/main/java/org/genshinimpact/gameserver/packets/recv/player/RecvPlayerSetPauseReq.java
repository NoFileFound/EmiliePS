package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_FAIL;
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerSetPauseRsp;

// Protocol buffers
import org.generated.protobuf.PlayerSetPauseReqOuterClass.PlayerSetPauseReq;

public final class RecvPlayerSetPauseReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = PlayerSetPauseReq.parseFrom(data);
        var myPlayer = session.getPlayer();
        if(myPlayer == null || myPlayer.getWorld() == null) {
            return;
        }

        if(myPlayer.getWorld().getPlayers().size() > 1) {
            session.sendPacket(new SendPlayerSetPauseRsp(RET_FAIL));
        } else {
            myPlayer.getWorld().setPaused(req.getIsPaused());
            session.sendPacket(new SendPlayerSetPauseRsp(RET_SUCC));
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PlayerSetPauseReq;
    }
}