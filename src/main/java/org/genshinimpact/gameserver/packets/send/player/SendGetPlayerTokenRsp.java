package org.genshinimpact.gameserver.packets.send.player;

// Imports
import com.google.protobuf.ByteString;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.enums.AccountType;

// Protocol buffers
import org.generated.protobuf.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp;

public final class SendGetPlayerTokenRsp implements SendPacket {
    private final byte[] data;

    public SendGetPlayerTokenRsp(Retcode retcode, String accountId, String psnId, String accountToken, Boolean isGuest, Integer platformType, Integer channelId, Integer subChannelId) {
        var proto =
            GetPlayerTokenRsp.newBuilder()
                .setAccountType(isGuest ? AccountType.ACCOUNT_GUEST.getValue() : AccountType.ACCOUNT_NORMAL.getValue())
                .setAccountUid(accountId)
                .setChannelId(channelId)
                .setIsGuest(isGuest)
                .setPlatformType(platformType)
                .setPsnId(psnId)
                .setRetcode(retcode.getValue())
                .setSubChannelId(subChannelId)
                .setToken(accountToken)
                .setUid(Integer.parseInt(accountId))
                .build();

        this.data = proto.toByteArray();
    }

    public SendGetPlayerTokenRsp(String accountId, String psnId, String accountToken, Integer platformType, Integer regPlatformType, Integer channelId, Integer subChannelId, String message, Long period) {
        var proto =
            GetPlayerTokenRsp.newBuilder()
                .setAccountType(AccountType.ACCOUNT_NORMAL.getValue())
                .setAccountUid(accountId)
                .setBlackUidEndTime(period)
                .setChannelId(channelId)
                .setIsGuest(false)
                .setMsg(message)
                .setPlatformType(platformType)
                .setPsnId(psnId)
                .setRegPlatform(regPlatformType)
                .setRetcode(Retcode.RET_BLACK_UID.getValue())
                .setSecretKeySeed(CryptoUtils.getClientSecretKeySeed())
                .setSecurityCmdBuffer(ByteString.copyFrom(CryptoUtils.getClientSecretKeyBuffer()))
                .setSubChannelId(subChannelId)
                .setToken(accountToken)
                .setUid(Integer.parseInt(accountId))
                .build();

        this.data = proto.toByteArray();
    }

    public SendGetPlayerTokenRsp(String accountId, String psnId, String accountToken, Boolean isGuest, Integer platformType, Integer regPlatformType, Integer channelId, Integer subChannelId, String encryptedSeed, String encryptedSeedSignature, String birthday, String countryCode, String clientIpAddress, Integer avatarsCount) {
        var proto =
            GetPlayerTokenRsp.newBuilder()
                .setAccountType(isGuest ? AccountType.ACCOUNT_GUEST.getValue() : AccountType.ACCOUNT_NORMAL.getValue())
                .setAccountUid(accountId)
                .setBirthday(birthday)
                .setChannelId(channelId)
                .setClientIpStr(clientIpAddress)
                .setClientVersionRandomKey("c25-314dd05b0b5f")
                .setCountryCode(countryCode.isEmpty() ? "US" : countryCode)
                .setIsGuest(isGuest)
                .setIsProficientPlayer(avatarsCount > 0)
                .setPlatformType(platformType)
                .setPsnId(psnId)
                .setRegPlatform(regPlatformType)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .setSecretKeySeed(CryptoUtils.getClientSecretKeySeed())
                .setSecurityCmdBuffer(ByteString.copyFrom(CryptoUtils.getClientSecretKeyBuffer()))
                .setServerRandKey(encryptedSeed)
                .setSign(encryptedSeedSignature)
                .setSubChannelId(subChannelId)
                .setToken(accountToken)
                .setUid(Integer.parseInt(accountId))
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.GetPlayerTokenRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}