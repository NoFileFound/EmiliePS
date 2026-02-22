package org.genshinimpact.webserver.routes.mdk;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Ticket;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.models.LoginModel;
import org.genshinimpact.webserver.responses.LoginResponse;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/shield/api", "hk4e_cn/mdk/shield/api", "mdk/shield/api", "takumi/hk4e_cn/mdk/shield/api", "takumi/hk4e_global/mdk/shield/api", "takumi/mdk/shield/api"}, produces = "application/json")
public final class MDKShieldController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code MDKShieldController}.
     * @param requestRateCache The rate limit cache.
     */
    public MDKShieldController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

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
            data.put("id", (clientType == ClientType.PLATFORM_IOS ? 4 : clientType == ClientType.PLATFORM_ANDROID ? 5 : clientType == ClientType.PLATFORM_PC ? 6 : clientType == ClientType.PLATFORM_PS4 ? 30 : clientType == ClientType.PLATFORM_ANDROID_CLOUD ? 27 : clientType == ClientType.PLATFORM_PC_CLOUD ? 53 : clientType == ClientType.PLATFORM_IOSCLOUD ? 26 : clientType == ClientType.PLATFORM_PS5 ? 28 : clientType == ClientType.PLATFORM_MACOSCLOUD ? 44 : clientType == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? 150 : clientType == ClientType.PLATFORM_DOUYIN_ANDROID_CLOUD ? 151 : 117));
            data.put("game_key", appName.getValue());
            data.put("client", (clientType == ClientType.PLATFORM_IOS ? "IOS" : clientType == ClientType.PLATFORM_ANDROID ? "Android" : clientType == ClientType.PLATFORM_PC ? "PC" : clientType == ClientType.PLATFORM_PS4 ? "PS" : clientType == ClientType.PLATFORM_ANDROID_CLOUD ? "CloudAndroid" : clientType == ClientType.PLATFORM_PC_CLOUD ? "CloudPC" : clientType == ClientType.PLATFORM_IOSCLOUD ? "CloudIOS" : clientType == ClientType.PLATFORM_PS5 ? "PS5" : clientType == ClientType.PLATFORM_MACOSCLOUD ? "CloudMacOS" : clientType == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? "CloudDouyiniOS" : clientType == ClientType.PLATFORM_DOUYIN_ANDROID_CLOUD ? "CloudDouyinAndroid" : clientType == ClientType.PLATFORM_CX ? "CX" : clientType == ClientType.PLATFORM_HARMONYOSNEXT ? "HarmonyOSNEXT" : ""));
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
            data.put("thirdparty_ignore", SpringBootApp.getWebConfig().mdkConfig.thirdparty_ignore);
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

    /**
     *  Source: <a href="https://hk4e-sdk.mihoyo.com/mdk/shield/api/login">https://hk4e-sdk.mihoyo.com/mdk/shield/api/login</a><br><br>
     *  Description: Logins in the game.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code account} — The account's email or nickname.</li>
     *          <li>{@code is_crypto} — Is the account password encrypted with AES256.</li>
     *          <li>{@code password} — The account's password.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "login")
    public ResponseEntity<Response<?>> SendMdkLogin(HttpServletRequest request, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 15) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        LoginModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), LoginModel.class);
            if(body.account == null || body.account.isBlank() || body.password == null || body.password.isBlank() || body.is_crypto == null || device_id == null || device_id.isBlank() || risky == null || risky.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(!SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }
            }

            if(!body.account.matches("^(?:[A-Za-z0-9][A-Za-z0-9._]{2,19}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_INVALID_FORMAT, "账号格式不正确"));
            }

            if(body.is_crypto == true) {
                body.password = CryptoUtils.decryptPassword(body.password);
                if(body.password.isEmpty()) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "密码解密错误"));
                }
            }

            if(!body.password.matches("^[!-~]{8,32}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "密碼格式為8-30位，並且至少包含數字、大小寫字母、英文特殊符號其中兩種"));
            }

            Account myAccount = (body.account.contains("@") ? DBUtils.findAccountByEmailAddress(body.account) : DBUtils.findAccountByUsername(body.account));
            if(myAccount == null || !myAccount.getPassword().equals(CryptoUtils.getMd5(body.password.getBytes()))) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_INVALID_ACCOUNT, "未找到账户"));
            }

            ///  TODO: HEARTBEAT SUPPORT.
            if(myAccount.getDeviceInfo().get(device_id) == null) {
                myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
                SpringBootApp.getTicketStore().createTicket(myAccount, Ticket.TicketType.TICKET_DEVICE_GRANT);
            } else {
                myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
                myAccount.save();
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new LoginResponse(myAccount, request.getRemoteAddr())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}