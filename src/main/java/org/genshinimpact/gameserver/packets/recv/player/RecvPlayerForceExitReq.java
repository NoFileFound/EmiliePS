package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.player.SendPlayerForceExitRsp;

public final class RecvPlayerForceExitReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        player.sendPacket(new SendPlayerForceExitRsp());
        player.closeConnection();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PlayerForceExitReq;
    }
}