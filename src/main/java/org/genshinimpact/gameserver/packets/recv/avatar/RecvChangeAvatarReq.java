package org.genshinimpact.gameserver.packets.recv.avatar;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.avatar.SendChangeAvatarRsp;

// Protocol buffers
import org.generated.protobuf.ChangeAvatarReqOuterClass.ChangeAvatarReq;

public class RecvChangeAvatarReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = ChangeAvatarReq.parseFrom(data);
        player.getAccount().getPlayerTeam().setCurrentAvatarEntity(req.getGuid());
        player.sendPacket(new SendChangeAvatarRsp(req.getGuid(), req.getSkillId()));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.ChangeAvatarReq;
    }
}