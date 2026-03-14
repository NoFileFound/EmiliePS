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
                .setRetcode(retcode.getValue())
                .setAccountUid(accountId)
                .setUid(Integer.parseInt(accountId))
                .setPsnId(psnId)
                .setToken(accountToken)
                .setIsGuest(isGuest)
                .setAccountType(isGuest ? AccountType.ACCOUNT_GUEST.getValue() : AccountType.ACCOUNT_NORMAL.getValue())
                .setPlatformType(platformType)
                .setChannelId(channelId)
                .setSubChannelId(subChannelId)
                .build();

        this.data = proto.toByteArray();
    }

    public SendGetPlayerTokenRsp(String accountId, String psnId, String accountToken, Integer platformType, Integer regPlatformType, Integer channelId, Integer subChannelId, String message, Integer period) {
        var proto =
            GetPlayerTokenRsp.newBuilder()
                .setRetcode(Retcode.RET_BLACK_UID.getValue())
                .setAccountUid(accountId)
                .setUid(Integer.parseInt(accountId))
                .setPsnId(psnId)
                .setToken(accountToken)
                .setIsGuest(false)
                .setMsg(message)
                .setBlackUidEndTime(period)
                .setAccountType(AccountType.ACCOUNT_NORMAL.getValue())
                .setSecretKeySeed(CryptoUtils.getClientSecretKeySeed())
                .setSecurityCmdBuffer(ByteString.copyFrom(CryptoUtils.getClientSecretKeyBuffer()))
                .setPlatformType(platformType)
                .setRegPlatform(regPlatformType)
                .setChannelId(channelId)
                .setSubChannelId(subChannelId)
                .build();

        this.data = proto.toByteArray();
    }

    public SendGetPlayerTokenRsp(String accountId, String psnId, String accountToken, Boolean isGuest, Integer platformType, Integer regPlatformType, Integer channelId, Integer subChannelId, String encryptedSeed, String encryptedSeedSignature, String birthday, String countryCode, String clientIpAddress, Integer avatarsCount) {
        var proto =
            GetPlayerTokenRsp.newBuilder()
                .setRetcode(Retcode.RET_SUCC.getValue())
                .setAccountUid(accountId)
                .setUid(Integer.parseInt(accountId))
                .setPsnId(psnId)
                .setToken(accountToken)
                .setIsGuest(isGuest)
                .setAccountType(isGuest ? AccountType.ACCOUNT_GUEST.getValue() : AccountType.ACCOUNT_NORMAL.getValue())
                .setSecretKeySeed(CryptoUtils.getClientSecretKeySeed())
                .setSecurityCmdBuffer(ByteString.copyFrom(CryptoUtils.getClientSecretKeyBuffer()))
                .setPlatformType(platformType)
                .setRegPlatform(regPlatformType)
                .setChannelId(channelId)
                .setSubChannelId(subChannelId)
                .setBirthday(birthday)
                .setIsProficientPlayer(avatarsCount > 0)
                .setClientVersionRandomKey("c25-314dd05b0b5f")
                .setClientIpStr(clientIpAddress)
                .setCountryCode(countryCode)
                .setSign(encryptedSeedSignature)
                .setServerRandKey(encryptedSeed)
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