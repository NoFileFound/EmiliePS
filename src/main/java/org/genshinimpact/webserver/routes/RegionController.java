package org.genshinimpact.webserver.routes;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_STOP_SERVER;
import static org.genshinimpact.gameserver.enums.Retcode.RET_CLIENT_FORCE_UPDATE;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.security.Signature;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import org.genshinimpact.Application;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.WebConfig;
import org.genshinimpact.webserver.enums.AccountType;
import org.genshinimpact.webserver.enums.ChannelType;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.SubChannelType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// Protocol buffers
import org.generated.protobuf.ForceUpdateInfoOuterClass.ForceUpdateInfo;
import org.generated.protobuf.RegionSimpleInfoOuterClass.RegionSimpleInfo;
import org.generated.protobuf.RegionInfoOuterClass.RegionInfo;
import org.generated.protobuf.ResVersionConfigOuterClass.ResVersionConfig;
import org.generated.protobuf.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp;
import org.generated.protobuf.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp;
import org.generated.protobuf.StopServerInfoOuterClass.StopServerInfo;

@RestController
public final class RegionController {
    private record RegionMap(WebConfig.RegionConfig regionClass, String base64) {}
    private String queryAllRegionResponse, queryAllRegionResponseOverseas;
    private final Map<String, RegionMap> serverRegions = new HashMap<>();
    private final Map<String, Set<String>> regionPrefixes = new HashMap<>();

    public RegionController() {
        var regionsConfig = SpringBootApp.getWebConfig().regionConfig;
        var dispatchKey = CryptoUtils.getDispatchKey();
        var dispatchSeed = CryptoUtils.getDispatchSeed();
        if(regionsConfig.isEmpty()) {
            Application.getLogger().severe("There are no game servers available. Exiting due to unplayable state.");
            System.exit(1);
        }

        List<RegionSimpleInfo> regions = new ArrayList<>(regionsConfig.size());
        for(var region : regionsConfig) {
            regions.add(RegionSimpleInfo.newBuilder().setName(region.name).setTitle(region.title).setType(region.regionType).setDispatchUrl(region.dispatchUrl).build());
            try {
                this.serverRegions.put(region.name, new RegionMap(region, CryptoUtils.encodeBase64(
                        QueryCurrRegionHttpRsp.newBuilder()
                                .setRetcode(0)
                                .setRegionInfo(buildRegionInfo(region))
                                .setClientSecretKey(ByteString.copyFrom(dispatchKey))
                                .setRegionCustomConfigEncrypted(ByteString.copyFrom(CryptoUtils.getXor(JsonUtils.toJsonString(region.encryptedConfig).getBytes(), dispatchKey)))
                                .setConnectGateTicket(region.dispatchTicket)
                                .build()
                                .toByteArray()
                )));
                this.regionPrefixes.put(region.name, region.dispatchVersions.stream().map(v -> v.replaceAll("[0-9.]+$", "")).collect(Collectors.toSet()));
            } catch(Exception ex) {
                throw new IllegalStateException("Failed building region response: " + region.name, ex);
            }
        }

        try {
            ObjectNode root = (ObjectNode)SpringBootApp.getWebConfig().regionGlobalConfig;
            if(root == null) {
                root = JsonNodeFactory.instance.objectNode();
            }

            this.queryAllRegionResponse = this.buildRegionList(regions, root, 1, dispatchKey, dispatchSeed);
            this.queryAllRegionResponseOverseas = this.buildRegionList(regions, root, 3, dispatchKey, dispatchSeed);
        } catch(Exception ex) {
            Application.getLogger().severe(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     *  Source: <a href="https://dispatchosglobal.yuanshen.com/query_region_list">https://dispatchosglobal.yuanshen.com/query_region_list</a><br><br>
     *  Description: Fetches the available region list.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code version} — The client's game version.</li>
     *          <li>{@code lang} — The client's game language id.</li>
     *          <li>{@code platform} — The client's platform type.</li>
     *          <li>{@code binary} — Fetch the list as binary or string.</li>
     *          <li>{@code time} — The milliseconds before going to fetch again.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code sub_channel_id} — The client's sub channel id.</li>
     *        </ul>
     */
    @GetMapping(value = "query_region_list")
    public Object SendQueryRegionList(String version, String lang, String platform, Boolean binary, String time, String channel_id, String sub_channel_id) {
        try {
            ClientType clientType = ClientType.fromValue(platform);
            ChannelType channelType = ChannelType.fromValue(channel_id);
            SubChannelType subChannelType = SubChannelType.fromValue(sub_channel_id);
            if(clientType == ClientType.PLATFORM_UNKNOWN || channelType == ChannelType.CHANNEL_UNKNOWN || lang == null || lang.isBlank() || subChannelType == SubChannelType.SUB_CHANNEL_UNKNOWN) {
                return "CP///////////wE=";
            }

            return (binary) ? (version.startsWith("CN") ? this.queryAllRegionResponse.getBytes() : this.queryAllRegionResponseOverseas.getBytes()) : (version.startsWith("CN") ? this.queryAllRegionResponse : this.queryAllRegionResponseOverseas);
        }catch(Exception ignored) {
            return "CP///////////wE=";
        }
    }

    /**
     *  Source: <a href="https://dispatchosglobal.yuanshen.com/query_cur_region">https://dispatchosglobal.yuanshen.com/query_cur_region</a><br><br>
     *  Description: Fetches the current region.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code version} — The client's game version.</li>
     *          <li>{@code lang} — The client's game language id.</li>
     *          <li>{@code platform} — The client's platform type.</li>
     *          <li>{@code binary} — Fetch the list as binary or string.</li>
     *          <li>{@code time} — The milliseconds before going to fetch again.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code sub_channel_id} — The client's sub channel id.</li>
     *          <li>{@code account_type} — The client's account type.</li>
     *          <li>{@code dispatchSeed} — The dispatch seed.</li>
     *          <li>{@code key_id} — The RSA key id.</li>
     *        </ul>
     */
    @GetMapping(value = "query_cur_region/{regionName}", produces = "application/json")
    public Object SendQueryCurRegion(String version, String lang, String platform, Boolean binary, String time, String channel_id, String sub_channel_id, String account_type, String dispatchSeed, String key_id, @PathVariable String regionName) {
        try {
            ClientType clientType = ClientType.fromValue(platform);
            ChannelType channelType = ChannelType.fromValue(channel_id);
            SubChannelType subChannelType = SubChannelType.fromValue(sub_channel_id);
            AccountType accountType = AccountType.fromValue(account_type);
            int keyId = Integer.parseInt(key_id);
            if(clientType == ClientType.PLATFORM_UNKNOWN || channelType == ChannelType.CHANNEL_UNKNOWN || lang == null || lang.isBlank() || subChannelType == SubChannelType.SUB_CHANNEL_UNKNOWN || accountType == AccountType.ACCOUNT_UNKNOWN || (accountType == AccountType.ACCOUNT_GUEST && !SpringBootApp.getWebConfig().mdkConfig.enable_guest)) {
                return "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
            }

            var region = this.serverRegions.get(regionName);
            if(region == null || !this.regionPrefixes.get(regionName).contains(version.replaceAll("[0-9.]+$", ""))) {
                return "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
            }

            String response;
            if(region.regionClass.maintenanceConfig != null) {
                response = this.buildCurRegionResponse(QueryCurrRegionHttpRsp.newBuilder()
                        .setRetcode(RET_STOP_SERVER.getValue())
                        .setMsg("Under Maintenance")
                        .setRegionInfo(RegionInfo.newBuilder().build())
                        .setStopServer(StopServerInfo.newBuilder()
                                .setUrl(region.regionClass.maintenanceConfig.url)
                                .setStopBeginTime(region.regionClass.maintenanceConfig.startDate)
                                .setStopEndTime(region.regionClass.maintenanceConfig.endDate)
                                .setContentMsg(region.regionClass.maintenanceConfig.msg)
                                .build())
                        .buildPartial()
                        .toByteArray(), keyId, dispatchSeed);

                return (binary) ? response.getBytes() : response;
            }

            if(!region.regionClass.dispatchVersions.contains(version)) {
                response = this.buildCurRegionResponse(
                        QueryCurrRegionHttpRsp.newBuilder()
                                .setRetcode(RET_CLIENT_FORCE_UPDATE.getValue())
                                .setMsg(String.format("Version update found. Please start the launcher to download the latest version.\n\nServer Version: %s\nClient Version: %s", region.regionClass.dispatchVersions.get(0).replaceAll("^[^0-9]+", ""), version.replaceAll("^[^0-9]+", "")))
                                .setRegionInfo(RegionInfo.newBuilder().build())
                                .setForceUpdate(ForceUpdateInfo.newBuilder()
                                        .setForceUpdateUrl("hoyoverse.com")
                                        .build())
                                .buildPartial()
                                .toByteArray(),
                        keyId,
                        dispatchSeed
                );

                return (binary) ? response.getBytes() : response;
            }

            response = this.buildCurRegionResponse(CryptoUtils.decodeBase64(region.base64), keyId, dispatchSeed);
            return (binary) ? response.getBytes() : response;
        } catch(Exception ignored) {
            return "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
        }
    }

    /**
     *  Source: <a href="https://dispatchosglobal.yuanshen.com/query_security_file">https://dispatchosglobal.yuanshen.com/query_security_file</a><br><br>
     *  Description: Fetches the dispatch's security key.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code file_key} — The client's game version.</li>
     *        </ul>
     */
    @GetMapping(value = "query_security_file")
    public String SendQuerySecurityFile(String file_key) {
        return "";
    }

    /**
     * A helper function to build response for region dispatch.
     * @param region The provided list of regions.
     * @param root The provided region config as JSON object.
     * @param sdkEnv The provided sdk environment id.
     * @param dispatchKey The provided dispatch key.
     * @param dispatchSeed The provided dispatch seed.
     * @return Base64-encoded protobuf of QueryRegionListHttpRsp.
     */
    private String buildRegionList(List<RegionSimpleInfo> region, ObjectNode root, int sdkEnv, byte[] dispatchKey, byte[] dispatchSeed) {
        root.put("sdkenv", sdkEnv);
        return CryptoUtils.encodeBase64(
                QueryRegionListHttpRsp.newBuilder()
                        .setRetcode(0)
                        .addAllRegionList(region)
                        .setClientSecretKey(ByteString.copyFrom(dispatchSeed))
                        .setClientCustomConfigEncrypted(ByteString.copyFrom(CryptoUtils.getXor(JsonUtils.toJsonString(root).getBytes(), dispatchKey)))
                        .setEnableLoginPc(true)
                        .build()
                        .toByteArray()
        );
    }

    /**
     * A helper function to build a {@code RegionInfo} protobuf.
     * @param region The provided region configuration.
     * @return A partially built {@code RegionInfo} protobuf instance.
     */
    private RegionInfo buildRegionInfo(WebConfig.RegionConfig region) {
        var rc = region.resourceConfig;
        RegionInfo.Builder b = RegionInfo.newBuilder();
        b.setGateserverIp(region.dispatchIp);
        b.setGateserverPort(region.dispatchPort);
        b.setGameBiz(rc.game_biz);
        b.setDataUrl(rc.data_url);
        b.setResourceUrl(rc.resource_url);
        b.setResourceUrlBak(rc.resource_url_bak);
        b.setNextResourceUrl(rc.next_resource_url);
        b.setClientDataVersion(rc.client_data_version);
        b.setClientSilenceDataVersion(rc.client_silence_data_version);
        b.setClientVersionSuffix(rc.client_version_suffix);
        b.setClientSilenceVersionSuffix(rc.client_silence_version_suffix);
        b.setAreaType(rc.area_type);
        b.setPayCallbackUrl(rc.pay_callback_url);
        b.setCdkeyUrl(rc.cdkey_url);
        b.setFeedbackUrl(rc.feedback_url);
        b.setPrivacyPolicyUrl(rc.privacy_policy_url);
        b.setAccountBindUrl(rc.account_bind_url);
        b.setHandbookUrl(rc.handbook_url);
        b.setOfficialCommunityUrl(rc.official_community_url);
        b.setBulletinUrl(rc.bulletin_url);
        b.setUserCenterUrl(rc.user_center_url);
        b.setClientDataMd5(JsonUtils.toJsonString(rc.client_data_md5));
        b.setClientSilenceDataMd5(JsonUtils.toJsonString(rc.client_silence_data_md5));
        if(rc.res_version_config != null) {
            b.setResVersionConfig(ResVersionConfig.newBuilder()
                    .setRelogin(rc.res_version_config.re_login)
                    .setMd5(JsonUtils.toJsonString(rc.res_version_config.md5))
                    .setVersion(rc.res_version_config.version)
                    .setReleaseTotalSize(rc.res_version_config.release_total_size)
                    .setVersionSuffix(rc.res_version_config.version_suffix)
                    .setBranch(rc.res_version_config.branch)
                    .buildPartial());
        }

        if(rc.next_res_version_config != null) {
            b.setNextResVersionConfig(ResVersionConfig.newBuilder()
                    .setRelogin(rc.next_res_version_config.re_login)
                    .setMd5(JsonUtils.toJsonString(rc.next_res_version_config.md5))
                    .setVersion(rc.next_res_version_config.version)
                    .setReleaseTotalSize(rc.next_res_version_config.release_total_size)
                    .setVersionSuffix(rc.next_res_version_config.version_suffix)
                    .setBranch(rc.next_res_version_config.branch)
                    .buildPartial());
        }

        return b.buildPartial();
    }

    /**
     * Encrypts the given region using RSA and signs it.
     * @param data The raw protobuf response bytes to encrypt and sign.
     * @param key_id The identifier used to select the RSA public encryption key.
     * @param dispatchSeed The dispatch seed associated with the response.
     * @return The JSON response of the encrypted content and signature.
     */
    private String buildCurRegionResponse(byte[] data, int key_id, String dispatchSeed) {
        if(key_id == 0) {
            return CryptoUtils.encodeBase64(data);
        }

        ///  TODO: INVESTIGATE dispatchSeed
        try {
            var keyId = CryptoUtils.getDispatchEncryptionKeys().get(key_id);
            if(keyId == null) {
                return "500";
            }

            ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyId);
            int chunkSize = 256 - 11;
            int length = data.length;
            int numChunks = (int) Math.ceil(length / (double) chunkSize);
            for(int i = 0; i < numChunks; i++) {
                byte[] chunk = Arrays.copyOfRange(data, i * chunkSize, Math.min((i + 1) * chunkSize, length));
                byte[] encryptedChunk = cipher.doFinal(chunk);
                encryptedStream.write(encryptedChunk);
            }

            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(CryptoUtils.getDispatchSignatureKey());
            privateSignature.update(data);
            return JsonUtils.toJsonString(new LinkedHashMap<>() {{
                put("content", CryptoUtils.encodeBase64(encryptedStream.toByteArray()));
                put("sign", CryptoUtils.encodeBase64(privateSignature.sign()));
            }});
        } catch(Exception ignored) {
            return "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
        }
    }
}