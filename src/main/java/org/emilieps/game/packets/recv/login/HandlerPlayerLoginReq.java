package org.emilieps.game.packets.recv.login;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.emilieps.Application;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.data.PacketRetcode;
import org.emilieps.game.connection.ClientSession;
import org.emilieps.game.connection.SessionState;
import org.emilieps.game.packets.base.InboundPacket;
import org.emilieps.game.packets.base.PacketHandler;
import org.emilieps.game.packets.base.PacketOpcode;
import org.emilieps.utils.DispatchUtils;

// Packets
import org.emilieps.game.packets.send.login.PacketPlayerLoginRsp;

// Protocol buffers
import generated.emilieps.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;

@SuppressWarnings("unused")
@PacketOpcode(PacketIdentifiers.Receive.PlayerLoginReq)
public final class HandlerPlayerLoginReq implements PacketHandler {
    @Override
    public void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException {
        PlayerLoginReq req = PlayerLoginReq.parseFrom(packet.getData());
        if(session.getState() != SessionState.WAITING_FOR_LOGIN) {
            session.sendPacket(new PacketPlayerLoginRsp(PacketRetcode.RET_ACCOUNT_VEIRFY_ERROR, req));
            return;
        }

        if(session.getPlayer() == null || !session.getPlayer().getAccount().getGameToken().equals(req.getToken())) {
            session.sendPacket(new PacketPlayerLoginRsp(PacketRetcode.RET_ACCOUNT_INFO_NOT_EXIST, req));
            return;
        }

        if(Application.getGameConfig().region.maintenance != null) {
            session.sendPacket(new PacketPlayerLoginRsp(Application.getGameConfig().region.maintenance, req));
            return;
        }

        if(!req.getClientVersionName().replaceAll(".*?(\\d+\\.\\d+\\.\\d+).*", "$1").equals(Application.getGameConfig().region.gateserver_version) || !DispatchUtils.isValidGameVersion(req.getClientVersionName())) {
            Application.getLogger().warn(Application.getTranslations().get("console", "clientvermismatch", req.getAccountUid(), req.getClientVersionName().replaceAll(".*?(\\d+\\.\\d+\\.\\d+).*", "$1"), Application.getGameConfig().region.gateserver_version));
            session.sendPacket(new PacketPlayerLoginRsp(PacketRetcode.RET_CLIENT_FORCE_UPDATE, req));
            return;
        }

        if(!req.getSecurityLibraryMd5().equals(Application.getGameConfig().security_md5_library)) {
            session.sendPacket(new PacketPlayerLoginRsp(PacketRetcode.RET_SECURITY_LIBRARY_ERROR, req));
            return;
        }

        session.sendPacket(new PacketPlayerLoginRsp(req));
    }
}