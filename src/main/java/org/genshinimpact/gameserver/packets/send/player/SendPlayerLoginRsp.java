package org.genshinimpact.gameserver.packets.send.player;

// Imports
import com.google.protobuf.ByteString;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.PlayerLoginRspOuterClass.PlayerLoginRsp;
import org.generated.protobuf.ResVersionConfigOuterClass.ResVersionConfig;

public final class SendPlayerLoginRsp implements SendPacket {
    private final byte[] data;

    public SendPlayerLoginRsp(Retcode retcode, String birthday, String countryCode, Integer targetUid, Long loginRand) {
        var proto =
            PlayerLoginRsp.newBuilder()
                .setBirthday(birthday)
                .setCountryCode(countryCode)
                .setLoginRand(loginRand)
                .setRetcode(retcode.getValue())
                .setTargetUid(targetUid)
                .build();

        this.data = proto.toByteArray();
    }

    public SendPlayerLoginRsp(String birthday, Integer clientDataVersion, Integer clientSilenceDataVersion, String clientVersionSuffix, String clientSilenceVersionSuffix, String clientMd5, String clientSilenceMd5, Long loginRand, Boolean isNewPlayer, Integer targetUid, Integer targetHomeUid, String countryCode, String registerCps, ResVersionConfig resourceVersionConfig) {
        var proto =
            PlayerLoginRsp.newBuilder()
                .setAbilityHashCode(1844674)
                .setBirthday(birthday)
                .setClientDataVersion(clientDataVersion)
                .setClientMd5(clientMd5)
                .setClientVersionSuffix(clientVersionSuffix)
                .setClientSilenceDataVersion(clientSilenceDataVersion)
                .setClientSilenceMd5(clientSilenceMd5)
                .setClientSilenceVersionSuffix(clientSilenceVersionSuffix)
                .setCountryCode(countryCode.isEmpty() ? "US" : countryCode)
                .setGameBiz("hk4e_cn")
                .setIsEnableClientHashDebug(true)
                .setIsNewPlayer(isNewPlayer)
                .setIsScOpen(true)
                .setIsUseAbilityHash(true)
                .setLoginRand(loginRand)
                .setRegisterCps(registerCps)
                .setResVersionConfig(resourceVersionConfig)
                .setRetcode(Retcode.RET_SUCC.getValue())
                .setScInfo(ByteString.copyFrom(new byte[] {}))
                .setTargetHomeOwnerUid(targetHomeUid)
                .setTargetUid(targetUid)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.PlayerLoginRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}