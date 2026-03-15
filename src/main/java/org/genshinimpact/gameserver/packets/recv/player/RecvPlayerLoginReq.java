package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.send.player.SendDoSetPlayerBornDataNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerLoginRsp;

// Protocol buffers
import org.generated.protobuf.ResVersionConfigOuterClass.ResVersionConfig;
import org.generated.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;

public class RecvPlayerLoginReq implements RecvPacket {
    @Override
    public void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = PlayerLoginReq.parseFrom(data);
        if(session.getState() != SessionState.WAITING_FOR_LOGIN || session.getPlayer() == null || !session.getPlayer().getPlayerIdentity().getComboToken().equals(req.getToken())) {
            session.sendPacket(new SendPlayerLoginRsp(Retcode.RET_ACCOUNT_VERIFY_ERROR, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
            return;
        }

        if(AppBootstrap.getMainConfig().badIPS.contains(session.getTunnel().getAddress().getAddress().getHostAddress())) {
            session.sendPacket(new SendPlayerLoginRsp(Retcode.RET_BLACK_LOGIN_IP, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
            return;
        }

        ///  TODO: INVESTIGATE SecurityCmdBuffer
        ///  TODO: INVESTIGATE WHY req.getAccountUid() is empty.

        if(!req.getSecurityLibraryMd5().equals("063c73b3f9a2a5293cb68cdfdad7c936")) {
            session.sendPacket(new SendPlayerLoginRsp(Retcode.RET_SECURITY_LIBRARY_ERROR, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
            return;
        }

        var isNewPlayer = session.getPlayer().getPlayerIdentity().getAvatars().isEmpty();
        if(isNewPlayer) {
            session.setState(SessionState.WAITING_FOR_PICKING_CHARACTER);
            session.sendPacket(new SendDoSetPlayerBornDataNotify());
        } else {
            session.getPlayer().sendLogin();
        }

        ///  TODO: DISPATCH RESOURCE CONFIG
        ResVersionConfig resVersionConfig = ResVersionConfig.newBuilder().build();
        session.sendPacket(new SendPlayerLoginRsp(req.getBirthday(), 0, 0, "", "", "", "", req.getLoginRand(), isNewPlayer, req.getTargetUid(), req.getTargetHomeOwnerUid(), req.getCountryCode(), req.getCps(), resVersionConfig));
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PlayerLoginReq;
    }
}

/*
        byte[] bytes = req.getSecurityCmdReply().toByteArray();
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) {
            hex.append(String.format("%02X", b));
        }

        System.out.println(hex);
 */