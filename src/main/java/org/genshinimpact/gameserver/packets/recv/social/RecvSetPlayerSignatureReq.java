package org.genshinimpact.gameserver.packets.recv.social;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SIGNATURE_ILLEGAL;
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.social.SendSetPlayerSignatureRsp;

// Protocol buffers
import org.generated.protobuf.SetPlayerSignatureReqOuterClass.SetPlayerSignatureReq;

public final class RecvSetPlayerSignatureReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        String signature = SetPlayerSignatureReq.parseFrom(data).getSignature();
        if(player.getAccount().isGuest()) {
            player.sendPacket(new SendSetPlayerSignatureRsp(Retcode.RET_FORBIDDEN));
            return;
        }

        if(signature.isBlank()) {
            player.sendPacket(new SendSetPlayerSignatureRsp(RET_SIGNATURE_ILLEGAL));
            return;
        }

        if(AppBootstrap.getMainConfig().badWords.stream().anyMatch(signature.toLowerCase().replaceAll("[^a-z0-9]", "")::contains)) {
            player.sendPacket(new SendSetPlayerSignatureRsp(RET_SIGNATURE_ILLEGAL));
            return;
        }

        player.getAccount().setProfileSignature(signature);
        player.sendPacket(new SendSetPlayerSignatureRsp(signature));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.SetPlayerSignatureReq;
    }
}