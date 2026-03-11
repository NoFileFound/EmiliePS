package org.genshinimpact.gameserver.packets.send.player;

import com.google.protobuf.ByteString;
import org.generated.protobuf.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.OutboundPacket;
import org.genshinimpact.utils.CryptoUtils;

public class SendGetPlayerTokenRsp extends OutboundPacket {

    public SendGetPlayerTokenRsp(ClientSession session, String accountId, String token, Retcode retcode, String encryptedSeed, String encryptedSeedSign) {
        super(198, true);

        this.setUseDispatchKey(true);

        GetPlayerTokenRsp p =
                GetPlayerTokenRsp.newBuilder()
                        .setUid(Integer.parseInt(accountId)) ///  (int)session.getUid()
                        .setToken(token)
                        .setRetcode(0)
                        .setAccountType(1)
                        .setIsProficientPlayer(false)
                        .setPlatformType(3)
                        .setChannelId(1)
                        .setClientVersionRandomKey("c25-314dd05b0b5f")
                        .setRegPlatform(3)
                        .setServerRandKey(encryptedSeed)
                        .setSign(encryptedSeedSign)
                        .build();

        this.setData(p.toByteArray());
    }
}