package org.genshinimpact.gameserver.packets.recv.player;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.ServerApp;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.webserver.routes.RegionController;
import org.genshinimpact.webserver.utils.JsonUtils;

// Packets
import org.genshinimpact.gameserver.packets.send.player.SendDoSetPlayerBornDataNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerLoginRsp;

// Protocol buffers
import org.generated.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;
import org.generated.protobuf.ResVersionConfigOuterClass.ResVersionConfig;

public final class RecvPlayerLoginReq implements RecvPacket {
    @Override
    public void handle(Server server, Player player, byte[] header, byte[] data) throws InvalidProtocolBufferException {
        var req = PlayerLoginReq.parseFrom(data);
        try {
            if(!player.getAccount().getComboToken().equals(req.getToken())) {
                player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_TOKEN_ERROR, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
                return;
            }

            ///  TODO: securityCmdBuffer.
            var regionInfo = RegionController.getRegionInfo(ServerApp.getGameConfig().regionName);
            if(regionInfo == null) {
                player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_FAIL, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
                return;
            }

            var resVersionConfig = ResVersionConfig.newBuilder();
            if(regionInfo.resourceConfig.res_version_config != null) {
                resVersionConfig
                    .setRelogin(regionInfo.resourceConfig.res_version_config.re_login)
                    .setMd5(JsonUtils.toJsonString(regionInfo.resourceConfig.res_version_config.md5))
                    .setVersion(regionInfo.resourceConfig.res_version_config.version)
                    .setReleaseTotalSize(regionInfo.resourceConfig.res_version_config.release_total_size)
                    .setVersionSuffix(regionInfo.resourceConfig.res_version_config.version_suffix)
                    .setBranch(regionInfo.resourceConfig.res_version_config.branch);
            }

            if(!regionInfo.dispatchVersions.contains(req.getChecksumClientVersion())) {
                player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_CLIENT_VERSION_ERROR, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
                return;
            }

            if(!req.getClientVerisonHash().equals("TpWYdoTmwsEGB0HG7sWWMDitsAA=")) {
                player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_CHECK_CLIENT_VERSION_HASH_FAIL, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
                return;
            }

            if(!req.getSecurityLibraryMd5().equals("063c73b3f9a2a5293cb68cdfdad7c936")) {
                player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_SECURITY_LIBRARY_ERROR, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
                return;
            }

            boolean isNewPlayer = (player.getAvatarStorage().getTotalAvatars() == 0);
            if(!isNewPlayer) {
                player.sendLogin();
            } else {
                if(player.getAccount().isGuest()) {
                    player.getAvatarStorage().addAvatar(10000005, true);
                    player.getAccount().setMainCharacterId(10000005);
                    player.getAccount().setProfileAvatarImageId(10000005);
                    player.getAccount().setUsername("Guest");
                    player.getAccount().save(true);
                    player.sendLogin();
                } else {
                    player.setSessionState(SessionState.WAITING_FOR_PICKING_CHARACTER);
                    player.sendPacket(new SendDoSetPlayerBornDataNotify());
                }
            }

            player.sendPacket(new SendPlayerLoginRsp(req.getBirthday(), regionInfo.resourceConfig.client_data_version, regionInfo.resourceConfig.client_silence_data_version, regionInfo.resourceConfig.client_version_suffix, regionInfo.resourceConfig.client_silence_version_suffix, JsonUtils.toJsonString(regionInfo.resourceConfig.client_data_md5), JsonUtils.toJsonString(regionInfo.resourceConfig.client_silence_data_md5), req.getLoginRand(), isNewPlayer, req.getTargetUid(), req.getTargetHomeOwnerUid(), req.getCountryCode(), req.getCps(), resVersionConfig.build()));
        } catch(Exception ex) {
            AppBootstrap.getLogger().error("OnPlayerLoginRsp : 23 [RET_LOGIN_INIT_FAIL]", ex);
            player.sendPacket(new SendPlayerLoginRsp(Retcode.RET_LOGIN_INIT_FAIL, req.getBirthday(), req.getCountryCode(), req.getTargetUid(), req.getLoginRand()));
        }
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Receive.PlayerLoginReq;
    }
}