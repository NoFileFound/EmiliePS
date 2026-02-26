package org.genshinimpact.webserver.routes.combo;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.enums.*;
import org.genshinimpact.webserver.models.combo.granter.*;
import org.genshinimpact.webserver.responses.combo.granter.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/combo/granter", "hk4e_cn/combo/granter", "combo/granter", "takumi/hk4e_global/combo/granter", "takumi/hk4e_cn/combo/granter", "takumi/combo/granter"}, produces = "application/json")
public final class ComboGranterController {
    private final Cache<String, AtomicInteger> requestRateCache;
    private static final Map<String, int[]> PROTOCOL_VERSIONS = Map.ofEntries(
            Map.entry("zh-cn", new int[]{57, 0}),
            Map.entry("zh-tw", new int[]{0, 1}),
            Map.entry("en", new int[]{18, 0}),
            Map.entry("ja", new int[]{15, 0}),
            Map.entry("ko", new int[]{16, 0}),
            Map.entry("de", new int[]{17, 0}),
            Map.entry("es", new int[]{15, 0}),
            Map.entry("fr", new int[]{16, 0}),
            Map.entry("id", new int[]{17, 0}),
            Map.entry("pt", new int[]{16, 0}),
            Map.entry("ru", new int[]{16, 0}),
            Map.entry("th", new int[]{17, 0}),
            Map.entry("vi", new int[]{17, 0}),
            Map.entry("tr", new int[]{7, 0}));

    /**
     * Creates a new {@code ComboGranterController}.
     * @param requestRateCache The rate limit cache.
     */
    public ComboGranterController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getConfig">https://devapi-takumi.mihoyo.com/combo/granter/api/getConfig</a><br><br>
     *  Description: Fetches configuration about the login screen.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "api/getConfig")
    public ResponseEntity<Response<?>> SendComboConfig(String app_id, String channel_id, String client_type) {
        try {
            AppId appId = AppId.fromValue(app_id);
            ChannelType channelType = ChannelType.fromValue(channel_id);
            ClientType clientType = ClientType.fromValue(client_type);
            if(appId == AppId.APP_UNKNOWN || channelType == ChannelType.CHANNEL_UNKNOWN || clientType == ClientType.PLATFORM_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "参数错误"));
            }

            GranterGetConfigResponse response = new GranterGetConfigResponse();
            response.setProtocol(channelType == ChannelType.CHANNEL_DEFAULT);
            response.setQrEnabled(SpringBootApp.getWebConfig().mdkConfig.enable_qrcode_login);
            response.setLogLevel("DEBUG");
            response.setAnnounceUrl(SpringBootApp.getWebConfig().mdkConfig.announce_url);
            response.setPushAliasType(SpringBootApp.getWebConfig().mdkConfig.push_alias_type);
            response.setDisableYsdkGuard(!SpringBootApp.getWebConfig().mdkConfig.enable_ysdk_guard);
            response.setEnableAnnouncePicPopup(SpringBootApp.getWebConfig().mdkConfig.enable_announce_pic_popup);
            response.setAppName("原神");
            if(clientType == ClientType.PLATFORM_PC || clientType == ClientType.PLATFORM_PC_CLOUD) {
                response.setQrEnabledApps(new GranterGetConfigResponse.QrApps(true, true));
                response.setQrAppIcons(new GranterGetConfigResponse.QrAppIcons("", "", "https://webstatic.mihoyo.com/upload/operation_location/2022/12/07/ec0f2514f044ac43754440241ab0b838_3962973103776517937.png"));
            }

            response.setQrCloudDisplayName("云·原神");
            response.setQrAppDisplayName("");
            response.setQrBbsDisplayName("");
            response.setEnableUserCenter(SpringBootApp.getWebConfig().mdkConfig.enable_user_center);
            response.setFunctionalSwitchConfigs(new GranterGetConfigResponse.FunctionalSwitchConfigs(SpringBootApp.getWebConfig().mdkConfig.enable_jpush, SpringBootApp.getWebConfig().mdkConfig.enable_appsflyer, false));
            response.setUgcProtocol(SpringBootApp.getWebConfig().mdkConfig.enable_ugc_protocol);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", response));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "参数错误"));
        }
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getDynamicClientConfig">https://devapi-takumi.mihoyo.com/combo/granter/api/getDynamicClientConfig</a><br><br>
     *  Description: Fetches dynamic configuration about the login screen.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Headers:<br>
     *        <ul>
     *          <li>{@code x-rpc-game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code x-rpc-client_type} — The client's platform type.</li>
     *        </ul>
     */
    @PostMapping(value = "api/getDynamicClientConfig")
    public ResponseEntity<Response<?>> SendComboDynamicConfig(@RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz, @RequestHeader(value = "x-rpc-client_type", required = false) String client_type, HttpServletRequest request) {
        try {
            ClientType clientType = ClientType.fromValue(client_type);
            AppName appName = AppName.fromValue(game_biz);
            if(clientType == null || clientType == ClientType.PLATFORM_UNKNOWN || appName == null || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "未知错误"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterGetDynamicConfigResponse(false, GeoIP.getCountryCode(request.getRemoteAddr()))));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "未知错误"));
        }
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getFont">https://devapi-takumi.mihoyo.com/combo/granter/api/getFont</a><br><br>
     *  Description: Fetches the fonts in the game.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *        </ul>
     */
    @GetMapping(value = "api/getFont")
    public ResponseEntity<Response<?>> SendComboFonts(String app_id) {
        try {
            AppId appName = AppId.fromValue(app_id);
            if(appName == null || appName == AppId.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "AppID错误"));
            }

            if(appName == AppId.APP_GENSHIN || appName == AppId.APP_CLOUDPLATFORM) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterGetFontResponse(List.of(
                        new GranterGetFontResponse.Font(
                                "0",
                                0,
                                "zh-cn.ttf",
                                "https://sdk.hoyoverse.com/sdk-public/2026/01/13/8a7c2fc13ca8ff4d46aad4e4d2e3b19e_3433964338697589248.ttf",
                                "8a7c2fc13ca8ff4d46aad4e4d2e3b19e"
                        ),
                        new GranterGetFontResponse.Font(
                                "0",
                                0,
                                "ja.ttf",
                                "https://sdk.hoyoverse.com/sdk-public/2026/01/13/2751f82ce50ca4ccfc14a0eb0db88a7a_4209864217558836390.ttf",
                                "2751f82ce50ca4ccfc14a0eb0db88a7a"
                        )
                ))));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterGetFontResponse()));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "AppID错误"));
        }
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/compareProtocolVersion">https://devapi-takumi.mihoyo.com/combo/granter/api/compareProtocolVersion</a><br><br>
     *  Description: Compares the client's protocol version with the server's expected version.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code language} — The client's operating system ISO 639-1 language code.</li>
     *          <li>{@code major} — The client's major protocol version.</li>
     *          <li>{@code minimum} — The minimum supported protocol version.</li>
     *        </ul>
     */
    @PostMapping(value = {"api/compareProtocolVersion", "api/getProtocol"})
    public ResponseEntity<Response<?>> SendComboProtocolVersion(HttpServletRequest request) {
        GranterCompareProtocolVersionModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), GranterCompareProtocolVersionModel.class);
            if(body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.channel_id == null || body.channel_id == ChannelType.CHANNEL_UNKNOWN || body.language == null || body.language.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
            }

            int[] version = PROTOCOL_VERSIONS.get(body.language);
            if(version == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败", null));
            }

            if((version[0] == body.major && version[1] == body.minimum)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterCompareProtocolVersionResponse()));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterCompareProtocolVersionResponse(true, 0, body.app_id, body.language, version[0], version[1])));
        }catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
        }
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/compareProtocolVersion">https://devapi-takumi.mihoyo.com/combo/granter/api/compareProtocolVersion</a><br><br>
     *  Description: Compares the client's protocol version with the server's expected version.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code language} — The client's operating system ISO 639-1 language code.</li>
     *          <li>{@code major} — The client's major protocol version.</li>
     *          <li>{@code minimum} — The minimum supported protocol version.</li>
     *        </ul>
     */
    @GetMapping(value = {"api/compareProtocolVersion", "api/getProtocol"})
    public ResponseEntity<Response<?>> SendComboProtocolVersion(@RequestParam(value = "app_id", required = false) String app_id, @RequestParam(value = "channel_id", required = false) String channel_id, @RequestParam(value = "language", required = false) String language, @RequestParam(value = "major", required = false) String major, @RequestParam(value = "minimum", required = false) String minimum) {
        try {
            AppId appId = AppId.fromValue(app_id);
            ChannelType channelType = ChannelType.fromValue(channel_id);
            int myMajor = Integer.parseInt(major);
            int myMinimum = Integer.parseInt(minimum);
            if(appId == AppId.APP_UNKNOWN || channelType == ChannelType.CHANNEL_UNKNOWN || language == null || language.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
            }

            int[] version = PROTOCOL_VERSIONS.get(language);
            if(version == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败", null));
            }

            if((version[0] == myMajor && version[1] == myMinimum)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterCompareProtocolVersionResponse()));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterCompareProtocolVersionResponse(true, 0, appId, language, version[0], version[1])));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/login/beforeVerify">https://devapi-takumi.mihoyo.com/combo/granter/login/beforeVerify</a><br><br>
     *  Description: Fetches the account's settings about additional verification.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code open_id} — The account id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code role} — The client's game region info.</li>
     *          <li>{@code time} — Time since last request.</li>
     *          <li>{@code combo_token} — The account session key.</li>
     *        </ul>
     */
    @PostMapping(value = "login/beforeVerify")
    public ResponseEntity<Response<?>> SendComboBeforeVerify(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        GranterBeforeVerifyModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), GranterBeforeVerifyModel.class);
            if(body.open_id == null || body.open_id.isBlank() || body.combo_token == null || body.combo_token.isBlank() || body.app_id == null || body.channel_id == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.open_id));
            if(myAccount == null || !myAccount.getComboToken().equals(body.combo_token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "账号错误"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterBeforeVerifyResponse(myAccount.getRequireHeartbeat(), myAccount.getRequireRealPerson())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/login/v2/login">https://devapi-takumi.mihoyo.com/combo/granter/login/v2/login</a><br><br>
     *  Description: Generates the game session.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code data} — The provided information needed for verification, like user id and variable to check if its guest.</li>
     *          <li>{@code device} — The client's device id.</li>
     *          <li>{@code sign} — The signature to check if the provided data is correct.</li>
     *        </ul>
     *  Headers:<br>
     *        <ul>
     *          <li>{@code x-rpc-game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     */
    @PostMapping(value = {"login/login", "login/v2/login"})
    public ResponseEntity<Response<?>> SendComboLogin(HttpServletRequest request, @RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz) {
        String ipAddress = request.getRemoteAddr();
        String countryCode = GeoIP.getCountryCode(ipAddress);
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        GranterLoginModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), GranterLoginModel.class);
            AppName appName = AppName.fromValue(game_biz);
            if(body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.channel_id == null || body.channel_id == ChannelType.CHANNEL_UNKNOWN || body.data == null || body.data.isBlank() || body.sign == null || body.sign.isBlank() || body.device == null || body.device.isBlank() || (appName != AppName.APP_GENSHIN && appName != AppName.APP_GENSHIN_OVERSEAS)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            String hmacSign = CryptoUtils.getHMAC256(String.format("app_id=%s&channel_id=%s&data=%s&device=%s", body.app_id.getValue(), body.channel_id.getValue(), body.data, body.device), (appName == AppName.APP_GENSHIN ? CryptoUtils.getComboKeys().get(1) : CryptoUtils.getComboKeys().get(3)));
            if(!hmacSign.equals(body.sign)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "签名错误"));
            }

            JsonNode data = JsonUtils.read(body.data);
            if(data == null || !data.has("uid")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            Long userId = data.get("uid").asLong();
            boolean isGuest = data.get("guest").asBoolean();
            if(isGuest) {
                var myGuest = DBUtils.getOrCreateGuest(body.device);
                if(myGuest == null) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
                }

                if(myGuest.getRequireHeartbeat()) {
                    Instant now = Instant.now();
                    Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(ipAddress, k -> now);
                    long elapsed = now.getEpochSecond() - start.getEpochSecond();
                    if(elapsed >= 5400) {
                        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ANTIADDICT_LOGIN, "已达到防沉迷限制"));
                    }
                }

                myGuest.setComboToken(CryptoUtils.generateStringKey(32));
                myGuest.save();
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterLoginResponse(String.valueOf(myGuest.getId()), myGuest.getComboToken(), myGuest.getRequireHeartbeat(), AccountType.ACCOUNT_GUEST, countryCode, myGuest.getIsNew(), null)));
            } else {
                String token = data.get("token").asText();
                var myAccount = DBUtils.findAccountById(userId);
                if(myAccount == null || myAccount.getSessionToken() == null || !myAccount.getSessionToken().equals(token) || myAccount.getRequireRealPerson() || myAccount.getRequireDeviceGrant() || myAccount.getRequireAccountReactivation() || myAccount.getRequireSafeMobile() || myAccount.getEmailBindTicket() != null) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                if(myAccount.getRequireHeartbeat()) {
                    Instant now = Instant.now();
                    Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(ipAddress, k -> now);
                    long elapsed = now.getEpochSecond() - start.getEpochSecond();
                    if(elapsed >= 5400) {
                        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ANTIADDICT_LOGIN, "已达到防沉迷限制"));
                    }
                }

                myAccount.setComboToken(CryptoUtils.generateStringKey(32));
                myAccount.save(true);
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GranterLoginResponse(String.valueOf(userId), myAccount.getComboToken(), myAccount.getRequireHeartbeat(), AccountType.ACCOUNT_NORMAL, countryCode, false, myAccount.getFatigueRemind())));
            }
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}