package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.PacketHandler;
import org.genshinimpact.gameserver.packets.send.player.SendGetPlayerTokenRsp;

// Protocol buffers
import org.generated.protobuf.GetPlayerTokenReqOuterClass.GetPlayerTokenReq;
import org.genshinimpact.utils.CryptoUtils;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.Signature;

public class RecvGetPlayerTokenReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        GetPlayerTokenReq req = GetPlayerTokenReq.parseFrom(packet.getData());

        session.setState(SessionState.WAITING_FOR_LOGIN);
        if (req.getKeyId() > 0) {
            var encryptSeed = CryptoUtils.getClientSecretKeySeed();
            try {
                var cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, CryptoUtils.getDispatchSignatureKey());

                var clientSeedEncrypted = CryptoUtils.decodeBase64(req.getClientRandKey());
                var clientSeed = ByteBuffer.wrap(cipher.doFinal(clientSeedEncrypted)).getLong();
                var seedBytes = ByteBuffer.wrap(new byte[8]).putLong(encryptSeed ^ clientSeed).array();

                cipher.init(Cipher.ENCRYPT_MODE, CryptoUtils.getDispatchEncryptionKeys().get(req.getKeyId()));
                var seedEncrypted = cipher.doFinal(seedBytes);

                var privateSignature = Signature.getInstance("SHA256withRSA");
                privateSignature.initSign(CryptoUtils.getDispatchSignatureKey());
                privateSignature.update(seedBytes);

                session.sendPacket(new SendGetPlayerTokenRsp(session, req.getAccountUid(), req.getAccountToken(), Retcode.RET_SUCC, CryptoUtils.encodeBase64(seedEncrypted), CryptoUtils.encodeBase64(privateSignature.sign())));
            }
            catch (Exception ignored) {
                session.sendPacket(new SendGetPlayerTokenRsp(session, req.getAccountUid(), req.getAccountToken(), Retcode.RET_ACCOUNT_VERIFY_ERROR, "", ""));
            }
        }
    }

    @Override
    public int getCode() {
        return 172;
    }
}