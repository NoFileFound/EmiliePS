package org.emilieps.game.packets.recv.login;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import org.emilieps.Application;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.data.PacketRetcode;
import org.emilieps.game.connection.ClientSession;
import org.emilieps.game.connection.SessionState;
import org.emilieps.game.player.Player;
import org.emilieps.game.packets.base.InboundPacket;
import org.emilieps.game.packets.base.PacketHandler;
import org.emilieps.game.packets.base.PacketOpcode;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.MongodbLib;

// Packets
import org.emilieps.game.packets.send.login.PacketGetPlayerTokenRsp;

// Protocol buffers
import generated.emilieps.protobuf.GetPlayerTokenReqOuterClass.GetPlayerTokenReq;

@SuppressWarnings("unused")
@PacketOpcode(PacketIdentifiers.Receive.GetPlayerTokenReq)
public final class HandlerGetPlayerTokenReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        GetPlayerTokenReq req = GetPlayerTokenReq.parseFrom(packet.getData());
        String clientIp = session.getTunnel().getAddress().getAddress().getHostAddress();
        var myAccount = MongodbLib.findAccountByToken(req.getAccountToken());
        try {
            if(myAccount == null || !Objects.equals(myAccount.get_id(), Long.parseLong(req.getAccountUid()))) {
                session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_LOGIN_DB_FAIL, req, clientIp));
                return;
            }
        } catch (NumberFormatException ignored) {
            session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_ACCOUNT_VEIRFY_ERROR, req, clientIp));
            return;
        }

        if(Application.getApplicationConfig().blacklist_ips.contains(clientIp)) {
            session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_BLACK_LOGIN_IP, req, clientIp));
            return;
        }

        if(!Application.getApplicationConfig().whitelist_ips.isEmpty() && !Application.getApplicationConfig().whitelist_ips.contains(clientIp)) {
            session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_STOP_REGISTER, req, clientIp));
            return;
        }

        if(!Application.getGameConfig().region.connect_gate_ticket.equals(req.getGateTicket())) {
            session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_GATE_TICKET_CHECK_ERROR, req, clientIp));
            return;
        }

        if(Application.getGameConfig().region.maintenance != null) {
            session.sendPacket(new PacketGetPlayerTokenRsp(Application.getGameConfig().region.maintenance, req, clientIp));
            return;
        }

        var exists = session.getServer().getPlayerByAccountId(myAccount.get_id());
        if(exists != null) {
            session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_ANOTHER_LOGIN, req, clientIp));
            return;
        }

        var mySanction = session.getServer().getAccountLatestSanction(myAccount.get_id());
        if(mySanction != null) {
            if(mySanction.getIsPermanent()) {
                session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_ACCOUNT_FREEZE, req, clientIp));
            } else if(mySanction.getState().equals("Active")) {
                session.sendPacket(new PacketGetPlayerTokenRsp(mySanction, req, clientIp));
            }
            return;
        }

        session.setState(SessionState.WAITING_FOR_LOGIN);
        session.setPlayer(new Player(session, myAccount));
        if(Application.getApplicationConfig().enable_encryption) {
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, EncryptionLib.getDispatchSignatureKey());

                byte[] clientSeedEncrypted = Base64.getDecoder().decode(req.getClientRandKey());
                long clientSeed = ByteBuffer.wrap(cipher.doFinal(clientSeedEncrypted)).getLong();
                byte[] seedBytes = ByteBuffer.wrap(new byte[8]).putLong(EncryptionLib.getSecretKeySeed() ^ clientSeed).array();

                cipher.init(Cipher.ENCRYPT_MODE, EncryptionLib.getEncryptionKeys().get(req.getKeyId()));
                byte[] seedEncrypted = cipher.doFinal(seedBytes);

                Signature privateSignature = Signature.getInstance("SHA256withRSA");
                privateSignature.initSign(EncryptionLib.getDispatchSignatureKey());
                privateSignature.update(seedBytes);

                session.sendPacket(new PacketGetPlayerTokenRsp(session.getUid(), myAccount, req, Base64.getEncoder().encodeToString(seedEncrypted), Base64.getEncoder().encodeToString(privateSignature.sign()), clientIp));
            } catch (Exception ex) {
                session.sendPacket(new PacketGetPlayerTokenRsp(PacketRetcode.RET_ACCOUNT_VEIRFY_ERROR, req, clientIp));
                Application.getLogger().error("Signature failure", ex);
            }
        } else {
            session.sendPacket(new PacketGetPlayerTokenRsp(session.getUid(), myAccount, req, "", "", clientIp));
        }
    }
}