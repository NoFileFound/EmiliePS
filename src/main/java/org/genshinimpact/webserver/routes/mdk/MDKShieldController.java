package org.genshinimpact.webserver.routes.mdk;

// Imports
import java.util.LinkedHashMap;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/shield/api", "hk4e_cn/mdk/shield/api", "mdk/shield/api", "takumi/hk4e_cn/mdk/shield/api", "takumi/hk4e_global/mdk/shield/api", "takumi/mdk/shield/api"}, produces = "application/json")
public final class MDKShieldController {
    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadConfig">https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadConfig</a><br><br>
     *  Description: Fetches client configuration about the login page.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code game_key} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code package_name} — The application's package name. (Android only)</li>
     *        </ul>
     */
    @GetMapping(value = "loadConfig")
    public ResponseEntity<Response<?>> SendMdkConfig(String client, String game_key) {
        try {
            ClientType clientType = ClientType.fromValue(client);
            AppName appName = AppName.fromValue(game_key);
            if(clientType == ClientType.PLATFORM_UNKNOWN || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "参数错误"));
            }

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("id", (clientType == ClientType.PLATFORM_IOS ? 4 : clientType == ClientType.PLATFORM_ANDROID ? 5 : clientType == ClientType.PLATFORM_PC ? 6 : clientType == ClientType.PLATFORM_PS4 ? 30 : clientType == ClientType.PLATFORM_ANDROIDCLOUD ? 27 : clientType == ClientType.PLATFORM_PCCLOUD ? 53 : clientType == ClientType.PLATFORM_IOSCLOUD ? 26 : clientType == ClientType.PLATFORM_PS5 ? 28 : clientType == ClientType.PLATFORM_MACOSCLOUD ? 44 : clientType == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? 150 : clientType == ClientType.PLATFORM_DOUYIN_ANDROIDCLOUD ? 151 : 117));
            data.put("game_key", appName.getValue());
            data.put("client", (clientType == ClientType.PLATFORM_IOS ? "IOS" : clientType == ClientType.PLATFORM_ANDROID ? "Android" : clientType == ClientType.PLATFORM_PC ? "PC" : clientType == ClientType.PLATFORM_PS4 ? "PS" : clientType == ClientType.PLATFORM_ANDROIDCLOUD ? "CloudAndroid" : clientType == ClientType.PLATFORM_PCCLOUD ? "CloudPC" : clientType == ClientType.PLATFORM_IOSCLOUD ? "CloudIOS" : clientType == ClientType.PLATFORM_PS5 ? "PS5" : clientType == ClientType.PLATFORM_MACOSCLOUD ? "CloudMacOS" : clientType == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? "CloudDouyiniOS" : clientType == ClientType.PLATFORM_DOUYIN_ANDROIDCLOUD ? "CloudDouyinAndroid" : clientType == ClientType.PLATFORM_CX ? "CX" : clientType == ClientType.PLATFORM_HARMONYOSNEXT ? "HarmonyOSNEXT" : ""));
            data.put("identity", "I_IDENTITY");
            data.put("guest", SpringBootApp.getWebConfig().mdkConfig.enable_guest);
            data.put("ignore_versions", SpringBootApp.getWebConfig().mdkConfig.ignore_versions);
            data.put("scene", (clientType == ClientType.PLATFORM_PC && appName == AppName.APP_GENSHIN ? "S_ACCOUNT" : "S_NORMAL"));
            data.put("name", (appName == AppName.APP_GENSHIN ? "原神" : "原神海外"));
            data.put("disable_regist", !SpringBootApp.getWebConfig().mdkConfig.enable_regist);
            data.put("enable_email_captcha", SpringBootApp.getWebConfig().mdkConfig.enable_email_captcha);
            data.put("thirdparty", SpringBootApp.getWebConfig().mdkConfig.thirdparty);
            data.put("disable_mmt", !SpringBootApp.getWebConfig().mdkConfig.enable_mtt);
            data.put("server_guest", SpringBootApp.getWebConfig().mdkConfig.enable_server_guest);
            data.put("enable_crash_sdk", SpringBootApp.getWebConfig().mdkConfig.enable_crash_sdk);
            //data.put("thirdparty_ignore", SpringBootApp.getWebConfig().mdkConfig.thirdparty_ignore);
            data.put("enable_ps_bind_account", SpringBootApp.getWebConfig().mdkConfig.enable_ps_bind_account);
            data.put("thirdparty_login_configs", SpringBootApp.getWebConfig().mdkConfig.thirdparty_login_configs);
            data.put("initialize_firebase", SpringBootApp.getWebConfig().mdkConfig.enable_firebase);
            data.put("bbs_auth_login", SpringBootApp.getWebConfig().mdkConfig.bbs_auth_login);
            data.put("bbs_auth_login_ignore", SpringBootApp.getWebConfig().mdkConfig.bbs_auth_login_ignore);
            data.put("fetch_instance_id", SpringBootApp.getWebConfig().mdkConfig.fetch_instance_id);
            data.put("enable_flash_login", SpringBootApp.getWebConfig().mdkConfig.enable_flash_login);
            data.put("enable_logo_18", SpringBootApp.getWebConfig().mdkConfig.enable_logo_18);
            data.put("logo_height", String.valueOf(SpringBootApp.getWebConfig().mdkConfig.enable_logo_18_height));
            data.put("logo_width", String.valueOf(SpringBootApp.getWebConfig().mdkConfig.enable_logo_18_width));
            data.put("enable_cx_bind_account", SpringBootApp.getWebConfig().mdkConfig.enable_cx_bind_account);
            data.put("firebase_blacklist_devices_switch", SpringBootApp.getWebConfig().mdkConfig.enable_firebase_device_switch);
            data.put("firebase_blacklist_devices_version", SpringBootApp.getWebConfig().mdkConfig.enable_firebase_device_switch ? 1 : 0);
            data.put("hoyolab_auth_login", SpringBootApp.getWebConfig().mdkConfig.hoyolab_auth_login);
            data.put("hoyolab_auth_login_ignore", SpringBootApp.getWebConfig().mdkConfig.hoyolab_auth_login_ignore);
            data.put("hoyoplay_auth_login", SpringBootApp.getWebConfig().mdkConfig.hoyoplay_auth_login);
            if(appName == AppName.APP_GENSHIN) {
                data.put("enable_douyin_flash_login", SpringBootApp.getWebConfig().mdkConfig.enable_douyin_flash_login);
                data.put("enable_age_gate", SpringBootApp.getWebConfig().mdkConfig.enable_age_gate);
                data.put("enable_age_gate_ignore", SpringBootApp.getWebConfig().mdkConfig.enable_age_gate_ignore);
            }
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", data));
        }catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR2, "缺少配置"));
        }
    }

    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadFirebaseBlackList">https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadFirebaseBlackList</a><br><br>
     *  Description: Fetches the device blacklist (firebase).<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code game_key} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *        </ul>
     */
    @GetMapping(value = "loadFirebaseBlackList")
    public ResponseEntity<Response<?>> SendMdkFireBase(String client, String game_key) {
        try {
            ClientType clientType = ClientType.fromValue(client);
            AppName appName = AppName.fromValue(game_key);
            if (clientType == ClientType.PLATFORM_UNKNOWN || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "参数错误"));
            }

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("device_blacklist_switch", SpringBootApp.getWebConfig().mdkConfig.enable_firebase_device_switch);
            data.put("device_blacklist_version", SpringBootApp.getWebConfig().mdkConfig.enable_firebase_device_switch ? 1 : 0);
            data.put("device_blacklist", JsonUtils.toJsonString(new LinkedHashMap<>() {{
                put("min_api", 28);
                put("device", JsonUtils.toJsonString(SpringBootApp.getWebConfig().mdkConfig.firebase_blacklist_devices));
                put("low_end_devices", JsonUtils.toJsonString(SpringBootApp.getWebConfig().mdkConfig.firebase_blacklist_lowenddevices));
            }}));
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", data));
        }catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR2, "缺少配置"));
        }
    }
}