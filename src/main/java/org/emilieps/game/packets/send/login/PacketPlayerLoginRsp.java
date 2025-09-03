package org.emilieps.game.packets.send.login;

// Imports
import com.google.protobuf.ByteString;
import org.emilieps.Application;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.data.PacketRetcode;
import org.emilieps.data.RegionClass;
import org.emilieps.game.packets.base.OutboundPacket;

// Protocol buffers
import generated.emilieps.protobuf.PlayerLoginReqOuterClass.PlayerLoginReq;
import generated.emilieps.protobuf.PlayerLoginRspOuterClass.PlayerLoginRsp;
import generated.emilieps.protobuf.StopServerInfoOuterClass.StopServerInfo;
import generated.emilieps.protobuf.ResVersionConfigOuterClass.ResVersionConfig;

public final class PacketPlayerLoginRsp extends OutboundPacket {
    /**
     * Creates a new response from the packet: PlayerLogin.
     *
     * @param retcode   The retcode to send.
     * @param request   The request from the packet: PlayerLogin.
     */
    public PacketPlayerLoginRsp(PacketRetcode retcode, PlayerLoginReq request) {
        super(PacketIdentifiers.Send.PlayerLoginRsp);

        PlayerLoginRsp proto =
                PlayerLoginRsp.newBuilder()
                        .setRetcode(retcode.getValue())
                        .setBirthday(request.getBirthday())
                        .setCountryCode(request.getCountryCode())
                        .setTargetUid(request.getTargetUid())
                        .setTargetHomeOwnerUid(request.getTargetHomeOwnerUid())
                        .setLoginRand(request.getLoginRand())
                        .setRegisterCps(request.getCps())
                        .build();

        this.setData(proto.toByteArray());
    }

    /**
     * Creates a new response from the packet: PlayerLogin.
     *
     * @param maintenance The game region's maintenance information.
     * @param request   The request from the packet: PlayerLogin.
     */
    public PacketPlayerLoginRsp(RegionClass.Maintenance maintenance, PlayerLoginReq request) {
        super(PacketIdentifiers.Send.PlayerLoginRsp);

        PlayerLoginRsp proto =
                PlayerLoginRsp.newBuilder()
                        .setRetcode(PacketRetcode.RET_STOP_SERVER.getValue())
                        .setStopServer(StopServerInfo.newBuilder()
                                .setContentMsg(maintenance.msg)
                                .setUrl(maintenance.url)
                                .setStopBeginTime(maintenance.startDate)
                                .setStopEndTime(maintenance.endDate)
                                .buildPartial())
                        .setBirthday(request.getBirthday())
                        .setCountryCode(request.getCountryCode())
                        .setTargetUid(request.getTargetUid())
                        .setTargetHomeOwnerUid(request.getTargetHomeOwnerUid())
                        .setLoginRand(request.getLoginRand())
                        .setRegisterCps(request.getCps())
                        .build();

        this.setData(proto.toByteArray());
    }

    /**
     * Creates a new response from the packet: PlayerLogin.
     *
     * @param request   The request from the packet: PlayerLogin.
     */
    public PacketPlayerLoginRsp(PlayerLoginReq request) {
        super(PacketIdentifiers.Send.PlayerLoginRsp);

        var region = Application.getGameConfig().region;
        PlayerLoginRsp.Builder proto = PlayerLoginRsp.newBuilder()
                .setRetcode(PacketRetcode.RET_SUCC.getValue())
                .setBirthday(request.getBirthday())
                .setCountryCode(request.getCountryCode())
                .setTargetUid(request.getTargetUid())
                .setTargetHomeOwnerUid(request.getTargetHomeOwnerUid())
                .setLoginRand(request.getLoginRand())
                .setIsTransfer(request.getIsTransfer())
                .setRegisterCps(request.getCps())
                .setGameBiz(region.resource_config.game_biz)
                /// TODO: .setAbilityHashCode(1844674)
                /// TODO: .setScInfo(ByteString.copyFrom(new byte[] {}))
                .setClientDataVersion(region.resource_config.client_data_version)
                .setClientSilenceDataVersion(region.resource_config.client_silence_data_version)
                .setClientDataMd5(region.resource_config.client_data_md5.toString().replace("[", "").replace("]", "").replace("},{", "}\r\n{"))
                .setClientSilenceDataMd5(region.resource_config.client_silence_data_md5.toString())
                .setResVersionConfig(ResVersionConfig.newBuilder()
                        .setRelogin(region.resource_config.res_version_config.re_login)
                        .setMd5(region.resource_config.res_version_config.md5.toString())
                        .setVersion(region.resource_config.res_version_config.version)
                        .setReleaseTotalSize(region.resource_config.res_version_config.release_total_size)
                        .setVersionSuffix(region.resource_config.res_version_config.version_suffix)
                        .setBranch(region.resource_config.res_version_config.branch)
                        .buildPartial())
                .setClientVersionSuffix(region.resource_config.client_version_suffix)
                .setClientSilenceVersionSuffix(region.resource_config.client_silence_version_suffix);

        if(region.resource_config.next_res_version_config != null) {
            proto.setNextResVersionConfig(ResVersionConfig.newBuilder()
                    .setRelogin(region.resource_config.next_res_version_config.re_login)
                    .setMd5(region.resource_config.next_res_version_config.md5.toString())
                    .setVersion(region.resource_config.next_res_version_config.version)
                    .setReleaseTotalSize(region.resource_config.next_res_version_config.release_total_size)
                    .setVersionSuffix(region.resource_config.next_res_version_config.version_suffix)
                    .setBranch(region.resource_config.next_res_version_config.branch)
                    .buildPartial());
        }

        this.setData(proto.build().toByteArray());
    }
}