package org.emilieps.bootspring.routes;

// Imports
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.security.Signature;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import org.emilieps.Application;
import org.emilieps.data.enums.AccountType;
import org.emilieps.data.enums.ChannelType;
import org.emilieps.data.enums.ClientType;
import org.emilieps.data.enums.SubChannelType;
import org.emilieps.libraries.EncryptionManager;
import org.emilieps.libraries.JsonLoader;
import org.emilieps.properties.configs.RegionConfig;
import org.springframework.web.bind.annotation.*;

// Protocol buffers
import generated.emilieps.protobuf.ForceUpdateInfoOuterClass.ForceUpdateInfo;
import generated.emilieps.protobuf.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp;
import generated.emilieps.protobuf.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp;
import generated.emilieps.protobuf.RegionInfoOuterClass.RegionInfo;
import generated.emilieps.protobuf.RegionSimpleInfoOuterClass.RegionSimpleInfo;
import generated.emilieps.protobuf.ResVersionConfigOuterClass.ResVersionConfig;
import generated.emilieps.protobuf.StopServerInfoOuterClass.StopServerInfo;

@RestController
public final class Region {
    private record RegionMap(RegionConfig.RegionConfigClass.RegionClass regionClass, String base64) {}
    private String queryAllRegionResponse, queryAllRegionResponseCN;
    private final Map<String, RegionMap> serverRegions = new HashMap<>();
    private final Integer maxTimeResponse = 15000;
    private final String[] SUPPORTED_VERSIONS = {
            "CNRELWin", "CNRELiOS", "CNRELAndroid", "CNRELPS4", "CNCBPS4",
            "CNRELPS5", "CNCBPS5", "CNGMWin", "CNGMiOS", "CNGMAndroid",
            "CNGMPS4", "CNGMPS5", "CNPREWin", "CNPREiOS", "CNPREAndroid",
            "CNPREPS4", "CNPREPS5", "CNINWin", "CNINiOS", "CNINAndroid",
            "OSRELWin", "OSRELiOS", "OSRELAndroid", "OSRELPS4SIEE", "OSRELPS4SIEA",
            "OSCBPS4", "OSCBPS4SIEE", "OSCBPS4SIEA", "OSRELPS5SIEE", "OSRELPS5SIEA",
            "OSCBPS5", "OSCBPS5SIEE", "OSCBPS5SIEA", "OSGMWin", "OSGMiOS",
            "OSGMAndroid", "OSGMPS4", "OSPREWin", "OSPREiOS", "OSPREAndroid",
            "OSPREPS4", "CNCBWin", "CNCBiOS", "CNCBAndroid", "OSCBWin",
            "OSCBiOS", "OSCBAndroid"
    };

    /**
     * Initializes the region server.
     */
    public Region() {
        ArrayList<RegionSimpleInfo> regions = new ArrayList<>();
        Application.getRegionInfo().regions.forEach(region -> {
            regions.add(RegionSimpleInfo.newBuilder().setName(region.gateserver_name).setTitle(region.gateserver_title).setType(region.gateserver_type).setDispatchUrl("http://127.0.0.1:8881/query_cur_region/" + region.gateserver_name).build());
            try {
                this.serverRegions.put(region.gateserver_name, new RegionMap(region, Base64.getEncoder().encodeToString(
                        QueryCurrRegionHttpRsp.newBuilder()
                                .setRetcode(RETCODE_SUCC.getValue())
                                .setRegionInfo(
                                        apply(RegionInfo.newBuilder(), b -> {
                                            b.setUseGateserverDomainName(!region.gateserver_domain_name.isEmpty());
                                            if (!region.gateserver_domain_name.isEmpty()) {
                                                b.setGateserverDomainName(region.gateserver_domain_name);
                                            } else {
                                                if (!region.gateserver_ipv6_ip.isEmpty()) {
                                                    b.setGateserverIpv6Ip(region.gateserver_ipv6_ip);
                                                } else {
                                                    b.setGateserverIp(region.gateserver_ip);
                                                }
                                                b.setGateserverPort(region.gateserver_port);
                                            }

                                            b.setGameBiz(region.resource_config.game_biz);
                                            b.setDataUrl(region.resource_config.data_url);
                                            b.setResourceUrl(region.resource_config.resource_url);
                                            b.setResourceUrlBak(region.resource_config.resource_url_bak);
                                            b.setNextResourceUrl(region.resource_config.next_resource_url);
                                            b.setClientDataVersion(region.resource_config.client_data_version);
                                            b.setClientSilenceDataVersion(region.resource_config.client_silence_data_version);
                                            b.setClientVersionSuffix(region.resource_config.client_version_suffix);
                                            b.setClientSilenceVersionSuffix(region.resource_config.client_silence_version_suffix);
                                            b.setAreaType(region.resource_config.area_type);
                                            b.setPayCallbackUrl(region.resource_config.pay_callback_url);
                                            b.setCdkeyUrl(region.resource_config.cdkey_url);
                                            b.setClientDataMd5(region.resource_config.client_data_md5.toString().replace("[", "").replace("]", "").replace("},{", "}\r\n{"));
                                            b.setClientSilenceDataMd5(region.resource_config.client_silence_data_md5.toString());
                                            b.setFeedbackUrl(region.resource_config.feedback_url);
                                            b.setPrivacyPolicyUrl(region.resource_config.privacy_policy_url);
                                            b.setAccountBindUrl(region.resource_config.account_bind_url);
                                            b.setHandbookUrl(region.resource_config.handbook_url);
                                            b.setOfficialCommunityUrl(region.resource_config.official_community_url);
                                            b.setBulletinUrl(region.resource_config.bulletin_url);
                                            b.setUserCenterUrl(region.resource_config.user_center_url);
                                            b.setResVersionConfig(
                                                    apply(ResVersionConfig.newBuilder(), r -> {
                                                        r.setRelogin(region.resource_config.res_version_config.re_login);
                                                        r.setMd5(region.resource_config.res_version_config.md5.toString());
                                                        r.setVersion(region.resource_config.res_version_config.version);
                                                        r.setReleaseTotalSize(region.resource_config.res_version_config.release_total_size);
                                                        r.setVersionSuffix(region.resource_config.res_version_config.version_suffix);
                                                        r.setBranch(region.resource_config.res_version_config.branch);
                                                    }).buildPartial()
                                            );

                                            if (region.resource_config.next_res_version_config != null) {
                                                b.setNextResVersionConfig(
                                                        apply(ResVersionConfig.newBuilder(), r -> {
                                                            r.setRelogin(region.resource_config.next_res_version_config.re_login);
                                                            r.setMd5(region.resource_config.next_res_version_config.md5.toString());
                                                            r.setVersion(region.resource_config.next_res_version_config.version);
                                                            r.setReleaseTotalSize(region.resource_config.next_res_version_config.release_total_size);
                                                            r.setVersionSuffix(region.resource_config.next_res_version_config.version_suffix);
                                                            r.setBranch(region.resource_config.next_res_version_config.branch);
                                                        }).buildPartial()
                                                );
                                            }
                                        }).build()
                                )
                                .setClientSecretKey(ByteString.copyFrom(EncryptionManager.getDispatchSeed()))
                                .setRegionCustomConfigEncrypted(ByteString.copyFrom(EncryptionManager.performXor(JsonLoader.toJson(region.custom_config).getBytes(), EncryptionManager.getDispatchKey())))
                                .setConnectGateTicket(region.connect_gate_ticket)
                                .build()
                                .toByteString().toByteArray())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            var root = JsonLoader.parseJsonSafe(JsonLoader.toJson(Application.getRegionInfo().custom_config));
            ((ObjectNode) root).put("sdkenv", 3);
            this.queryAllRegionResponse = Base64.getEncoder().encodeToString(
                    QueryRegionListHttpRsp.newBuilder()
                            .setRetcode(RETCODE_SUCC.getValue())
                            .addAllRegionList(regions)
                            .setClientSecretKey(ByteString.copyFrom(EncryptionManager.getDispatchSeed()))
                            .setClientCustomConfigEncrypted(ByteString.copyFrom(EncryptionManager.performXor(JsonLoader.toJson(root).getBytes(), EncryptionManager.getDispatchKey())))
                            .setEnableLoginPc(Application.getRegionInfo().enable_login_pc)
                            .build()
                            .toByteString().toByteArray());

            ((ObjectNode) root).put("sdkenv", 1);
            this.queryAllRegionResponseCN = Base64.getEncoder().encodeToString(
                    QueryRegionListHttpRsp.newBuilder()
                            .setRetcode(RETCODE_SUCC.getValue())
                            .addAllRegionList(regions)
                            .setClientSecretKey(ByteString.copyFrom(EncryptionManager.getDispatchSeed()))
                            .setClientCustomConfigEncrypted(ByteString.copyFrom(EncryptionManager.performXor(JsonLoader.toJson(root).getBytes(), EncryptionManager.getDispatchKey())))
                            .setEnableLoginPc(Application.getRegionInfo().enable_login_pc)
                            .build()
                            .toByteString().toByteArray());

            Application.getLogger().info(Application.getTranslationManager().get("console", "loadedregions", this.serverRegions.size()));
        } catch (Exception e) {
            this.queryAllRegionResponse = "";
            this.queryAllRegionResponseCN = "";
            Application.getLogger().info(Application.getTranslationManager().get("console", "unablereloadregions"));
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
     *          <li>{@code time} — The milliseconds before going to fetch.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code sub_channel_id} — The client's sub channel id.</li>
     *        </ul>
     */
    @GetMapping(value = "query_region_list")
    public Object SendQueryRegionList(String version, Integer lang, ClientType platform, Boolean binary, Integer time, ChannelType channel_id, SubChannelType sub_channel_id, HttpServletRequest request) {
        if(version == null || channel_id == null || binary == null || platform == null || platform == ClientType.PLATFORM_UNKNOWN || channel_id == ChannelType.CHANNEL_UNKNOWN || sub_channel_id == null || sub_channel_id == SubChannelType.SUB_CHANNEL_UNKNOWN || time == null) {
            return "CP///////////wE=";
        }

        if (Stream.of(this.SUPPORTED_VERSIONS).noneMatch(version::startsWith) || time > this.maxTimeResponse) {
            this.queryAllRegionResponse = this.queryAllRegionResponseCN = "CP///////////wE=";
        }

        Application.getLogger().info(Application.getTranslationManager().get("console", "requestedallregions", request.getRemoteAddr()));
        return (binary) ? (version.startsWith("CN") ? this.queryAllRegionResponseCN.getBytes() : this.queryAllRegionResponse.getBytes()) : (version.startsWith("CN") ? this.queryAllRegionResponseCN : this.queryAllRegionResponse);
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
     *          <li>{@code time} — The milliseconds before going to fetch.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code sub_channel_id} — The client's sub channel id.</li>
     *          <li>{@code account_type} — The client's account type.</li>
     *          <li>{@code dispatchSeed} — The dispatch seed.</li>
     *          <li>{@code key_id} — The RSA key id.</li>
     *        </ul>
     */
    @GetMapping(value = "query_cur_region/{regionName}", produces = "application/json")
    public Object SendQueryCurRegion(String version, Integer lang, ClientType platform, Boolean binary, Integer time, ChannelType channel_id, SubChannelType sub_channel_id, AccountType account_type, String dispatchSeed, Integer key_id, @PathVariable String regionName, HttpServletRequest request) {
        if (version == null || platform == null || platform == ClientType.PLATFORM_UNKNOWN || time > this.maxTimeResponse
                || binary == null || channel_id == null || channel_id == ChannelType.CHANNEL_UNKNOWN
                || sub_channel_id == null || sub_channel_id == SubChannelType.SUB_CHANNEL_UNKNOWN
                || dispatchSeed == null || key_id == null
                || account_type == null || account_type == AccountType.ACCOUNT_UNKNOWN) {
            return "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
        }

        var region = this.serverRegions.get(regionName);
        version = version.replaceFirst("^[A-Za-z]+", "");
        byte[] responseBytes;
        if (region == null) {
            responseBytes = QueryCurrRegionHttpRsp.newBuilder()
                    .setRetcode(7)
                    .setMsg("No config")
                    .setRegionInfo(RegionInfo.newBuilder().build())
                    .build()
                    .toByteArray();

        } else if (region.regionClass.maintenance != null) {
            responseBytes = QueryCurrRegionHttpRsp.newBuilder()
                    .setRetcode(11)
                    .setMsg("Under Maintenance")
                    .setRegionInfo(RegionInfo.newBuilder().build())
                    .setStopServer(StopServerInfo.newBuilder()
                            .setUrl(region.regionClass.maintenance.url)
                            .setStopBeginTime(region.regionClass.maintenance.startDate)
                            .setStopEndTime(region.regionClass.maintenance.endDate)
                            .setContentMsg(region.regionClass.maintenance.msg)
                            .build())
                    .buildPartial()
                    .toByteArray();

        } else if(!version.equals(region.regionClass.gateserver_version)) {
            responseBytes = QueryCurrRegionHttpRsp.newBuilder()
                    .setRetcode(20)
                    .setMsg(String.format("Version update found. Please start the launcher to download the latest version.\n\nServer Version: %s\nClient Version: %s", region.regionClass.gateserver_version, version))
                    .setRegionInfo(RegionInfo.newBuilder().build())
                    .setForceUpdate(ForceUpdateInfo.newBuilder()
                            .setForceUpdateUrl("hoyoverse.com")
                            .build())
                    .buildPartial()
                    .toByteArray();
        } else {
            Application.getLogger().info(Application.getTranslationManager().get("console", "requestedcurrregion", request.getRemoteAddr(), regionName));
            responseBytes = Base64.getDecoder().decode(region.base64);
        }

        try {
            ByteArrayOutputStream encryptedStream = new ByteArrayOutputStream();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, EncryptionManager.getEncryptionKeys().get(key_id));
            int chunkSize = 256 - 11;
            int length = responseBytes.length;
            int numChunks = (int) Math.ceil(length / (double) chunkSize);
            for (int i = 0; i < numChunks; i++) {
                byte[] chunk = Arrays.copyOfRange(responseBytes, i * chunkSize, Math.min((i + 1) * chunkSize, length));
                byte[] encryptedChunk = cipher.doFinal(chunk);
                encryptedStream.write(encryptedChunk);
            }

            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(EncryptionManager.getDispatchSignatureKey());
            privateSignature.update(responseBytes);
            String response = JsonLoader.toJson(new LinkedHashMap<>() {{
                put("content", Base64.getEncoder().encodeToString(encryptedStream.toByteArray()));
                put("sign", Base64.getEncoder().encodeToString(privateSignature.sign()));
            }});

            return (binary) ? response.getBytes() : response;
        } catch (Exception e) {
            Application.getLogger().error(e.getMessage());
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
        if (file_key == null || Stream.of(this.SUPPORTED_VERSIONS).noneMatch(file_key::startsWith)) {
            return "";
        }
        return new String(EncryptionManager.getDispatchSecurityKey());
    }


    // Methods
    public <T> T apply(T obj, Consumer<T> consumer) {
        consumer.accept(obj);
        return obj;
    }
}