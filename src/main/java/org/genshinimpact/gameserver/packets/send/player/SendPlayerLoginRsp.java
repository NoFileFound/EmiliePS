package org.genshinimpact.gameserver.packets.send.player;

// Imports
import com.google.protobuf.ByteString;
import org.genshinimpact.gameserver.packets.OutboundPacket;

// Protocol buffers
import org.generated.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;
import org.generated.protobuf.PlayerLoginRspOuterClass.PlayerLoginRsp;

public class SendPlayerLoginRsp extends OutboundPacket {
    public SendPlayerLoginRsp(PlayerLoginReq req) {
        super(135);

        PlayerLoginRsp p =
                PlayerLoginRsp.newBuilder()
                        .setIsUseAbilityHash(true) // true
                        .setAbilityHashCode(1844674) // 1844674
                        .setGameBiz("hk4e_global")
                        .setClientDataVersion(req.getClientDataVersion())
                        .setClientSilenceDataVersion(0)
                        .setClientMd5("")
                        .setClientSilenceMd5("")
                        //.setResVersionConfig(req.getResVersionConfig())
                        .setClientVersionSuffix("")
                        .setClientSilenceVersionSuffix("")
                        .setIsScOpen(false)
                        .setScInfo(ByteString.copyFrom(new byte[] {}))
                        .setRegisterCps("mihoyo")
                        .setCountryCode("US")
                        .build();

        this.setData(p.toByteArray());
    }
}