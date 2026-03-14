package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.time.Instant;
import javax.crypto.Cipher;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.player.SendGetPlayerTokenRsp;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.ClientType;

// Protocol buffers
import org.generated.protobuf.GetPlayerTokenReqOuterClass.GetPlayerTokenReq;

public final class RecvGetPlayerTokenReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = GetPlayerTokenReq.parseFrom(data);
        try {
            boolean isGuest = req.getIsGuest();
            ClientType myPlatform = ClientType.fromValue(String.valueOf(req.getPlatformType()));
            if(myPlatform == ClientType.PLATFORM_UNKNOWN) {
                session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_UNKNOWN_PLATFORM, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            var myIdentity = isGuest ? DBUtils.findGuestById(Long.parseLong(req.getAccountUid())) : DBUtils.findAccountById(Long.parseLong(req.getAccountUid()));
            if(myIdentity == null) {
                session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ACCOUNT_NOT_EXIST, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(!myIdentity.getComboToken().equals(req.getAccountToken())) {
                session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_TOKEN_ERROR, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(myIdentity instanceof Account account) {
                if(account.getRequireRealPerson() || account.getRequireDeviceGrant() || account.getRequireSafeMobile() || account.getEmailBindTicket() != null) {
                    session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_FORBIDDEN, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                    return;
                }

                ///  TODO: SANCTION LOGS
            }

            if(session.getServer().getTotalPlayers() + 1 > AppBootstrap.getMainConfig().maximumPlayers) {
                session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_MAX_PLAYER, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(myIdentity.getRequireHeartbeat()) {
                Instant now = Instant.now();
                Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(session.getTunnel().getAddress().getAddress().getHostAddress(), k -> now);
                long elapsed = now.getEpochSecond() - start.getEpochSecond();
                if(elapsed >= 5400) {
                    session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ANTI_ADDICT, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                    return;
                }
            }

            var myPlayer = session.getServer().getPlayer(myIdentity.getId(), isGuest ? Player.PlayerType.GUEST : Player.PlayerType.ACCOUNT);
            if(myPlayer != null) {
                myPlayer.closeConnection();
                session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ANOTHER_LOGIN, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(myIdentity instanceof Account account) {
                myPlayer = new Player(account, session);
            } else {
                myPlayer = new Player((Guest)myIdentity, session);
            }

            session.setPlayer(myPlayer);
            if(req.getKeyId() > 0) {
                var encryptSeed = CryptoUtils.getClientSecretKeySeed();
                try {
                    var cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.DECRYPT_MODE, CryptoUtils.getDispatchSignatureKey());
                    var clientSeed = ByteBuffer.wrap(cipher.doFinal(CryptoUtils.decodeBase64(req.getClientRandKey()))).getLong();
                    var seedBytes = ByteBuffer.wrap(new byte[8]).putLong(encryptSeed ^ clientSeed).array();
                    cipher.init(Cipher.ENCRYPT_MODE, CryptoUtils.getDispatchEncryptionKeys().get(req.getKeyId()));
                    var privateSignature = Signature.getInstance("SHA256withRSA");
                    privateSignature.initSign(CryptoUtils.getDispatchSignatureKey());
                    privateSignature.update(seedBytes);
                    session.sendPacket(new SendGetPlayerTokenRsp(req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), 3, req.getChannelId(), req.getSubChannelId(), CryptoUtils.encodeBase64(cipher.doFinal(seedBytes)), CryptoUtils.encodeBase64(privateSignature.sign()), req.getBirthday(), req.getCountryCode(), session.getTunnel().getAddress().getAddress().getHostAddress(), myPlayer.getPlayerIdentity().getAvatars().size()));
                } catch(Exception ignored) {
                    session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ACCOUNT_VERIFY_ERROR, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                    session.getTunnel().close();
                }
            }
        } catch(Exception ignored) {
            session.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_LOGIN_INIT_FAIL, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.GetPlayerTokenReq;
    }
}