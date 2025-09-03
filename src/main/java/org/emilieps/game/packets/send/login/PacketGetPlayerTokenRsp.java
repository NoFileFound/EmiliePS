package org.emilieps.game.packets.send.login;

// Imports
import com.google.protobuf.ByteString;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.data.PacketRetcode;
import org.emilieps.data.RegionClass;
import org.emilieps.database.Account;
import org.emilieps.database.Sanction;
import org.emilieps.game.packets.base.OutboundPacket;

// Libraries
import org.emilieps.library.EncryptionLib;

// Protocol buffers
import generated.emilieps.protobuf.GetPlayerTokenReqOuterClass.GetPlayerTokenReq;
import generated.emilieps.protobuf.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp;
import generated.emilieps.protobuf.StopServerInfoOuterClass.StopServerInfo;

public final class PacketGetPlayerTokenRsp extends OutboundPacket {
    /**
     * Creates a new response from the packet: GetPlayerToken.
     *
     * @param retcode   The retcode to send.
     * @param request   The request from the packet: GetPlayerToken.
     * @param ipAddress The session's ip address.
     */
    public PacketGetPlayerTokenRsp(PacketRetcode retcode, GetPlayerTokenReq request, String ipAddress) {
        super(PacketIdentifiers.Send.GetPlayerTokenRsp, true, true);

        GetPlayerTokenRsp proto =
                GetPlayerTokenRsp.newBuilder()
                        .setRetcode(retcode.getValue())
                        .setAccountUid(request.getAccountUid())
                        .setPsnId(request.getPsnId())
                        .setClientIpStr(ipAddress)
                        .setChannelId(request.getChannelId())
                        .setSubChannelId(request.getSubChannelId())
                        .setPlatformType(request.getPlatformType())
                        .build();

        this.setData(proto.toByteArray());
    }

    /**
     * Creates a new response from the packet: GetPlayerToken
     *
     * @param maintenance The game region's maintenance information.
     * @param request     The request from the packet: GetPlayerToken.
     * @param ipAddress   The session's ip address.
     */
    public PacketGetPlayerTokenRsp(RegionClass.Maintenance maintenance, GetPlayerTokenReq request, String ipAddress) {
        super(PacketIdentifiers.Send.GetPlayerTokenRsp, true, true);

        GetPlayerTokenRsp proto =
                GetPlayerTokenRsp.newBuilder()
                        .setRetcode(PacketRetcode.RET_STOP_SERVER.getValue())
                        .setStopServer(StopServerInfo.newBuilder()
                                .setContentMsg(maintenance.msg)
                                .setUrl(maintenance.url)
                                .setStopBeginTime(maintenance.startDate)
                                .setStopEndTime(maintenance.endDate)
                                .buildPartial())
                        .setAccountUid(request.getAccountUid())
                        .setPsnId(request.getPsnId())
                        .setClientIpStr(ipAddress)
                        .setChannelId(request.getChannelId())
                        .setSubChannelId(request.getSubChannelId())
                        .setPlatformType(request.getPlatformType())
                        .build();

        this.setData(proto.toByteArray());
    }

    /**
     * Creates a new response from the packet: GetPlayerToken
     *
     * @param sanction  Information about the session's current sanction.
     * @param request   The request from the packet: GetPlayerToken.
     * @param ipAddress The session's ip address.
     */
    public PacketGetPlayerTokenRsp(Sanction sanction, GetPlayerTokenReq request, String ipAddress) {
        super(PacketIdentifiers.Send.GetPlayerTokenRsp, true, true);

        GetPlayerTokenRsp proto =
                GetPlayerTokenRsp.newBuilder()
                        .setRetcode(PacketRetcode.RET_BLACK_UID.getValue())
                        .setBlackUidEndTime(Math.toIntExact(sanction.getExpirationDate()))
                        .setMsg(sanction.getReason().toString())
                        .setAccountUid(request.getAccountUid())
                        .setPsnId(request.getPsnId())
                        .setClientIpStr(ipAddress)
                        .setChannelId(request.getChannelId())
                        .setSubChannelId(request.getSubChannelId())
                        .setPlatformType(request.getPlatformType())
                        .build();

        this.setData(proto.toByteArray());
    }

    /**
     * Creates a new response from the packet: GetPlayerToken
     *
     * @param uid  The player's id.
     * @param account The session's account.
     * @param encryptionSeed The encrypted seed.
     * @param encryptionSeedSignature The encrypted seed signature.
     * @param request   The request from the packet: GetPlayerToken.
     * @param ipAddress The session's ip address.
     */
    public PacketGetPlayerTokenRsp(int uid, Account account, GetPlayerTokenReq request, String encryptionSeed, String encryptionSeedSignature, String ipAddress) {
        super(PacketIdentifiers.Send.GetPlayerTokenRsp, true, true);

        GetPlayerTokenRsp.Builder proto =
                GetPlayerTokenRsp.newBuilder()
                        .setRetcode(PacketRetcode.RET_SUCC.getValue())
                        .setUid(uid)
                        .setAccountUid(request.getAccountUid())
                        .setAccountType(request.getAccountType())
                        .setIsGuest(request.getIsGuest())
                        .setPlatformType(request.getPlatformType())
                        .setRegPlatform(account.getRegPlatform())
                        .setGameBiz(account.getLastGameBiz())
                        .setToken(account.getGameToken())
                        .setChannelId(request.getChannelId())
                        .setSubChannelId(request.getSubChannelId())
                        .setCountryCode(request.getCountryCode())
                        .setBirthday(request.getBirthday())
                        .setClientIpStr(ipAddress)
                        .setAuthAppid(request.getAuthAppid())
                        .setAuthkey(EncryptionLib.sha256Encode(account.getGameToken()))
                        .setPsnId(request.getPsnId())
                        .setMinorsRegMinAge(request.getMinorsRegMinAge())
                        .setKeyId(request.getKeyId())
                        .setClientVersionRandomKey("c25-314dd05b0b5f")
                        .setSecretKeySeed(EncryptionLib.getSecretKeySeed())
                        /// TODO: .setIsProficientPlayer()
                        .setSecurityCmdBuffer(ByteString.copyFrom(EncryptionLib.getSecretKey()));

        if (!encryptionSeed.isEmpty() && !encryptionSeedSignature.isEmpty()) {
            proto.setServerRandKey(encryptionSeed).setSign(encryptionSeedSignature);
        }

        this.setData(proto.build().toByteArray());
    }
}