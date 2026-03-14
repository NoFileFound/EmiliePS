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
                .setRetcode(retcode.getValue())
                .setBirthday(birthday)
                .setCountryCode(countryCode)
                .setTargetUid(targetUid)
                .setLoginRand(loginRand)
                .build();

        this.data = proto.toByteArray();
    }

    public SendPlayerLoginRsp(String birthday, Integer clientDataVersion, Integer clientSilenceDataVersion, String clientVersionSuffix, String clientSilenceVersionSuffix, String clientMd5, String clientSilenceMd5, Long loginRand, Boolean isNewPlayer, Integer targetUid, Integer targetHomeUid, String countryCode, String registerCps, ResVersionConfig resourceVersionConfig) {
        var proto =
            PlayerLoginRsp.newBuilder()
                .setRetcode(Retcode.RET_SUCC.getValue())
                .setBirthday(birthday)
                .setIsUseAbilityHash(true)
                .setIsEnableClientHashDebug(true)
                .setAbilityHashCode(1844674)
                .setLoginRand(loginRand)
                .setGameBiz("hk4e_global")
                .setClientDataVersion(clientDataVersion)
                .setClientSilenceDataVersion(clientSilenceDataVersion)
                .setClientVersionSuffix(clientVersionSuffix)
                .setClientSilenceVersionSuffix(clientSilenceVersionSuffix)
                .setClientMd5(clientMd5)
                .setClientSilenceMd5(clientSilenceMd5)
                .setResVersionConfig(resourceVersionConfig)
                .setIsNewPlayer(isNewPlayer)
                .setTargetUid(targetUid)
                .setTargetHomeOwnerUid(targetHomeUid)
                .setIsRelogin(false)
                .setIsScOpen(true)
                .setScInfo(ByteString.copyFrom(new byte[] {}))
                .setCountryCode(countryCode)
                .setRegisterCps(registerCps)
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