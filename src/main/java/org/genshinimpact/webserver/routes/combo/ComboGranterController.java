package org.genshinimpact.webserver.routes.combo;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.genshinimpact.libraries.GeoIP;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.*;
import org.genshinimpact.webserver.models.GetProtocolModel;
import org.genshinimpact.webserver.responses.GetProtocolResponse;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/combo/granter", "hk4e_cn/combo/granter", "combo/granter", "takumi/hk4e_global/combo/granter", "takumi/hk4e_cn/combo/granter", "takumi/combo/granter"}, produces = "application/json")
public final class ComboGranterController {
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
    public ResponseEntity<Response<?>> SendMDKConfig(String app_id, String channel_id, String client_type) {
        try {
            AppId appId = AppId.fromValue(app_id);
            ChannelType channelType = ChannelType.fromValue(channel_id);
            ClientType clientType = ClientType.fromValue(client_type);
            if(appId == AppId.APP_UNKNOWN || channelType == ChannelType.CHANNEL_UNKNOWN || clientType == ClientType.PLATFORM_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "参数错误"));
            }

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("protocol", channelType == ChannelType.CHANNEL_DEFAULT);
            data.put("qr_enabled", SpringBootApp.getWebConfig().mdkConfig.enable_qrcode_login);
            data.put("log_level", "DEBUG");
            data.put("announce_url", SpringBootApp.getWebConfig().mdkConfig.announce_url);
            data.put("push_alias_type", SpringBootApp.getWebConfig().mdkConfig.push_alias_type);
            data.put("disable_ysdk_guard", !SpringBootApp.getWebConfig().mdkConfig.enable_ysdk_guard);
            data.put("enable_announce_pic_popup", SpringBootApp.getWebConfig().mdkConfig.enable_announce_pic_popup);
            data.put("app_name", "原神");
            if(clientType == ClientType.PLATFORM_PC || clientType == ClientType.PLATFORM_PCCLOUD) {
                data.put("qr_enabled_apps", new LinkedHashMap<>() {{
                    put("bbs", true);
                    put("cloud", true);
                }});
                data.put("qr_app_icons", new LinkedHashMap<>() {{
                    put("app", "");
                    put("bbs", "");
                    put("cloud", "https://webstatic.mihoyo.com/upload/operation_location/2022/12/07/ec0f2514f044ac43754440241ab0b838_3962973103776517937.png");
                }});
            }

            data.put("qr_cloud_display_name", "云·原神");
            data.put("qr_app_display_name", "");
            data.put("qr_bbs_display_name", "");
            data.put("enable_user_center", SpringBootApp.getWebConfig().mdkConfig.enable_user_center);
            data.put("functional_switch_configs", new LinkedHashMap<>() {{
                put("jpush", SpringBootApp.getWebConfig().mdkConfig.enable_jpush);
                put("initialize_appsflyer", SpringBootApp.getWebConfig().mdkConfig.enable_appsflyer);
                put("allow_notification", false);
            }});
            data.put("ugc_protocol", SpringBootApp.getWebConfig().mdkConfig.enable_ugc_protocol);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", data));
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
    public ResponseEntity<Response<?>> SendDynamicConfig(@RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz, @RequestHeader(value = "x-rpc-client_type", required = false) String client_type, HttpServletRequest request) {
        try {
            ClientType clientType = ClientType.fromValue(client_type);
            AppName appName = AppName.fromValue(game_biz);
            if(clientType == null || clientType == ClientType.PLATFORM_UNKNOWN || appName == null || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "未知错误"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("enable_consent_banner", false);
                put("region_code", GeoIP.getCountryCode(request.getRemoteAddr()));
            }}));
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
    public ResponseEntity<Response<?>> SendFonts(String app_id) {
        try {
            AppId appName = AppId.fromValue(app_id);
            if(appName == null || appName == AppId.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "AppID错误"));
            }

            if(appName == AppId.APP_GENSHIN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", Map.of(
                        "fonts", List.of(
                                Map.of(
                                        "font_id", "0",
                                        "app_id", 0,
                                        "name", "zh-cn.ttf",
                                        "url", "https://sdk.hoyoverse.com/sdk-public/2026/01/13/8a7c2fc13ca8ff4d46aad4e4d2e3b19e_3433964338697589248.ttf",
                                        "md5", "8a7c2fc13ca8ff4d46aad4e4d2e3b19e"
                                ),
                                Map.of(
                                        "font_id", "0",
                                        "app_id", 0,
                                        "name", "ja.ttf",
                                        "url", "https://sdk.hoyoverse.com/sdk-public/2026/01/13/2751f82ce50ca4ccfc14a0eb0db88a7a_4209864217558836390.ttf",
                                        "md5", "2751f82ce50ca4ccfc14a0eb0db88a7a"
                                )
                        )
                )));
            }
            else
            {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", Map.of("fonts", List.of())));
            }
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
    public ResponseEntity<Response<?>> SendProtocolVersion(HttpServletRequest request) {
        GetProtocolModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), GetProtocolModel.class);
            if(body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.channel_id == null || body.channel_id == ChannelType.CHANNEL_UNKNOWN || body.language == null || body.language.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
            }

            int[] version = PROTOCOL_VERSIONS.get(body.language);
            if(version == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败", null));
            }

            if((version[0] == body.major && version[1] == body.minimum)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GetProtocolResponse()));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GetProtocolResponse(true, 0, body.app_id, body.language, version[0], version[1])));
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
    public ResponseEntity<Response<?>> SendProtocolVersion(@RequestParam(value = "app_id", required = false) String app_id, @RequestParam(value = "channel_id", required = false) String channel_id, @RequestParam(value = "language", required = false) String language, @RequestParam(value = "major", required = false) String major, @RequestParam(value = "minimum", required = false) String minimum) {
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
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GetProtocolResponse()));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GetProtocolResponse(true, 0, appId, language, version[0], version[1])));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "协议加载失败"));
        }
    }
}