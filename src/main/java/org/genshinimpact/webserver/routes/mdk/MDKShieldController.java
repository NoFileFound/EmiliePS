package org.genshinimpact.webserver.routes.mdk;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Ticket;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.enums.ClientApiActionType;
import org.genshinimpact.webserver.models.mdk.shield.*;
import org.genshinimpact.webserver.responses.mdk.shield.*;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.SMSUtils;
import org.genshinimpact.webserver.utils.SMTPUtils;
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
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/actionTicket">https://devapi-takumi.mihoyo.com/mdk/shield/api/actionTicket</a><br><br>
     *  Description: Fetches the ticket id for client's action.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code account_id} — The client's account id.</li>
     *          <li>{@code game_token} — The client's game token.</li>
     *          <li>{@code action_type} — The client's action type.</li>
     *        </ul>
     */
    @PostMapping(value = "actionTicket")
    public ResponseEntity<Response<?>> SendMdkActionTicket(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldActionTicketModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldActionTicketModel.class);
            if(body == null || body.action_type == null || body.action_type == ClientApiActionType.CLIENT_API_ACTION_TYPE_UNKNOWN || body.account_id == null || body.account_id.isBlank() || body.game_token == null || body.game_token.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.account_id));
            if(myAccount == null || !myAccount.getSessionToken().equals(body.game_token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            Ticket myTicket;
            String bindingType;
            switch(body.action_type) {
                case CLIENT_API_ACTION_TYPE_BIND_EMAIL:
                    myTicket = DBUtils.getTicketByAccountId(myAccount.getId(), Ticket.TicketType.TICKET_BIND_EMAIL);
                    bindingType = "Email";
                    break;
                case CLIENT_API_ACTION_TYPE_BIND_REALNAME:
                    myTicket = SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_BIND_REALNAME);
                    bindingType = "Realname";
                    break;
                case CLIENT_API_ACTION_TYPE_MODIFY_REALNAME:
                    myTicket = SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_MODIFY_REALNAME);
                    bindingType = "Realname";
                    break;
                case CLIENT_API_ACTION_TYPE_BIND_MOBILE:
                    myTicket = SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_BIND_MOBILE);
                    myTicket.setData(myAccount.getEmailAddress());
                    bindingType = "Mobile";
                    break;
                default:
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(myTicket == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            AppBootstrap.getLogger().info("[{} Binding] The {} binding started on account: {} | with verification code: {}.", bindingType, bindingType, myAccount.getId(), myTicket.getVerCode());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldActionTicketResponse(myTicket.getId())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/bindEmail">https://devapi-takumi.mihoyo.com/mdk/shield/api/bindEmail</a><br><br>
     *  Description: Binds the client's email address.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code email} — The client's new email address.</li>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code captcha} — The verification code.</li>
     *        </ul>
     */
    @PostMapping(value = "bindEmail")
    public ResponseEntity<Response<?>> SendMdkBindEmail(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldBindEmailModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldBindEmailModel.class);
            if(body == null || body.action_ticket == null || body.action_ticket.isBlank() || body.email == null || body.email.isBlank() || body.captcha == null || body.captcha.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(!body.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "邮箱地址无效"));
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null || !myTicket.getType().equals(Ticket.TicketType.TICKET_BIND_EMAIL)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null || !myAccount.getEmailBindTicket().equals(body.action_ticket)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(!myTicket.getVerCode().equals(body.captcha)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "验证码错误"));
            }

            myAccount.setEmailAddress(body.email);
            myAccount.setEmailBindTicket(null);
            SpringBootApp.getTicketStore().removeTicket(myTicket, myAccount);
            myAccount.save(true);
            AppBootstrap.getLogger().info("[Email Binding] The email binding ended successfully on account: {}.", myAccount.getId());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptcha">https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptcha</a><br><br>
     *  Description: Verifies the new email address of the client.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_type} — The action type.</li>
     *          <li>{@code email} — The client's email address to bind.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "emailCaptcha")
    public ResponseEntity<Response<?>> SendMdkEmailCaptcha(HttpServletRequest request, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldEmailCaptchaModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldEmailCaptchaModel.class);
            if(body == null || body.email == null || body.email.isBlank() || body.action_type == null || body.action_type == ClientApiActionType.CLIENT_API_ACTION_TYPE_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
            }

            if(!body.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "邮箱地址无效"));
            }

            var myAccount = DBUtils.findAccountByEmailAddress(body.email);
            var myTicket = SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_BIND_EMAIL);
            String verCode = "%06d".formatted(new java.security.SecureRandom().nextInt(1000000));
            myTicket.setVerCode(verCode);
            myAccount.setEmailBindTicket(myTicket.getId());
            myAccount.save(true);
            AppBootstrap.getLogger().info("[Email Binding] The email binding started on account: {} | with verification code: {}.", myAccount.getId(), myTicket.getVerCode());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptchaByActionTicket">https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptchaByActionTicket</a><br><br>
     *  Description: Sends captcha to email address from given ticket id.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code action_type} — The client's action type.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "emailCaptchaByActionTicket")
    public ResponseEntity<Response<?>> SendMdkEmailCaptchaByActionTicket(HttpServletRequest request, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldEmailCaptchaByActionTicketModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldEmailCaptchaByActionTicketModel.class);
            if(body == null || body.action_ticket == null || body.action_ticket.isBlank() || body.action_type == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            String verCode = "%06d".formatted(new java.security.SecureRandom().nextInt(1000000));
            myTicket.setVerCode(verCode);
            SMTPUtils.sendDeviceVerificationEmailMessage((String)myTicket.getData(), verCode);
            AppBootstrap.getLogger().info("[Binding] The mobile binding started on account: {} | with verification code: {}.", myTicket.getData(), verCode);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
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
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldLoginModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldLoginModel.class);
            if(body == null || body.account == null || body.account.isBlank() || body.password == null || body.password.isBlank() || body.is_crypto == null || device_id == null || device_id.isBlank() || risky == null || risky.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
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

            var myAccount = (body.account.contains("@") ? DBUtils.findAccountByEmailAddress(body.account) : DBUtils.findAccountByUsername(body.account));
            if(myAccount == null || !myAccount.getPassword().equals(CryptoUtils.getMd5(body.password.getBytes()))) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_INVALID_ACCOUNT, "未找到账户"));
            }

            myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
            if(myAccount.getDeviceInfo().get(device_id) == null) {
                SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_DEVICE_GRANT);
            } else {
                if(myAccount.getIsPendingDeletion()) {
                    SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_REACTIVATE_ACCOUNT);
                } else {
                    myAccount.save(false);
                }
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldLoginResponse(myAccount, ipAddress)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/loginCaptcha">https://devapi-takumi.mihoyo.com/mdk/shield/api/loginCaptcha</a><br><br>
     *  Description: Generates a captcha for login using a mobile number.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code area} — The client's mobile number area.</li>
     *          <li>{@code mobile} — The client's mobile number.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "loginCaptcha")
    public ResponseEntity<Response<?>> SendMdkLoginCaptcha(HttpServletRequest request, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldLoginCaptchaModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldLoginCaptchaModel.class);
            if(body == null || body.area == null || body.area.isBlank() || body.mobile == null || body.mobile.isEmpty()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(!body.area.equals("+86") || !(body.area + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "请输入正确的手机号码"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
            }

            var myAccount = DBUtils.findAccountByMobile(body.mobile);
            if(myAccount == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_INVALID_ACCOUNT, "未找到账户"));
            }

            var myTicket = SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_MOBILE_LOGIN);
            String verCode = "%06d".formatted(new java.security.SecureRandom().nextInt(1000000));
            myTicket.setVerCode(verCode);
            SMSUtils.sendSMS(body.area + body.mobile, verCode);
            AppBootstrap.getLogger().info("[Binding] The mobile login started on account: {} | with verification code: {}.", myAccount.getId(), myTicket.getVerCode());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldLoginCaptchaResponse("Login")));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/loginMobile">https://devapi-takumi.mihoyo.com/mdk/shield/api/loginMobile</a><br><br>
     *  Description: Logins in the game using mobile.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action} — The action name (It is always Login).</li>
     *          <li>{@code area} — The client's mobile number area.</li>
     *          <li>{@code mobile} — The client's mobile number.</li>
     *          <li>{@code captcha} — The verification code.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *        </ul>
     */
    @PostMapping(value = "loginMobile")
    public ResponseEntity<Response<?>> SendMdkLoginMobile(HttpServletRequest request, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldLoginMobileModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldLoginMobileModel.class);
            if(body == null || body.mobile == null || body.mobile.isEmpty() || body.area == null || body.area.isBlank() || body.captcha == null || body.captcha.isBlank() || body.action == null || body.action.isBlank() || !body.action.equals("Login")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myAccount = DBUtils.findAccountByMobile(body.mobile);
            if(myAccount == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            var myTicket = DBUtils.getTicketByAccountId(myAccount.getId(), Ticket.TicketType.TICKET_MOBILE_LOGIN);
            if(myTicket == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            if(!myTicket.getVerCode().equals(body.captcha)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "验证码错误"));
            }

            myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
            if(myAccount.getDeviceInfo().get(device_id) == null) {
                SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_DEVICE_GRANT);
            } else {
                if(myAccount.getIsPendingDeletion()) {
                    SpringBootApp.getTicketStore().getOrCreateTicket(myAccount, Ticket.TicketType.TICKET_REACTIVATE_ACCOUNT);
                } else {
                    myAccount.save(false);
                }
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldLoginResponse(myAccount, ipAddress)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/mobileCaptcha">https://devapi-takumi.mihoyo.com/mdk/shield/api/mobileCaptcha</a><br><br>
     *  Description: Sends captcha to the mobile to verify it.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_type} — The client's action type.</li>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code mobile} — The client's mobile number.</li>
     *          <li>{@code safe_mobile} — Is safe mobile or a normal mobile.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "mobileCaptcha")
    public ResponseEntity<Response<?>> SendMdkMobileCaptcha(HttpServletRequest request, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldMobileCaptchaModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldMobileCaptchaModel.class);
            if(body == null || body.safe_mobile == null || body.mobile == null || body.mobile.isBlank() || body.action_ticket == null || body.action_ticket.isBlank() || body.action_type == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
            }

            if(("+86" + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "请输入正确的手机号码"));
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null || !myTicket.getType().equals(Ticket.TicketType.TICKET_BIND_MOBILE)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            if(DBUtils.findAccountByMobile(body.mobile) != null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "手机号已被使用"));
            }

            String verCode = "%06d".formatted(new java.security.SecureRandom().nextInt(1000000));
            myTicket.setVerCode(verCode);
            SMSUtils.sendSMS("+86" + body.mobile, verCode);
            AppBootstrap.getLogger().info("[Binding] The mobile bind started on mobile: +86{} | with verification code: {}.", body.mobile, myTicket.getVerCode());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/reactivateAccount">https://devapi-takumi.mihoyo.com/mdk/shield/api/reactivateAccount</a><br><br>
     *  Description: Reactivates the account by given ticket id.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id for reactivation.</li>
     *        </ul>
     */
    @PostMapping(value = "reactivateAccount")
    public ResponseEntity<Response<?>> SendMdkReactivateAccount(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldReactivateAccountModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldReactivateAccountModel.class);
            if(body == null || body.action_ticket == null || body.action_ticket.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null || !myTicket.getType().equals(Ticket.TicketType.TICKET_REACTIVATE_ACCOUNT)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null || !myAccount.getRequireAccountReactivationTicket().equals(body.action_ticket)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
            myAccount.setIsPendingDeletion(false);
            SpringBootApp.getTicketStore().removeTicket(myTicket, myAccount);
            myAccount.save(true);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldLoginResponse(myAccount, ipAddress)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/verify">https://devapi-takumi.mihoyo.com/mdk/shield/api/verify</a><br><br>
     *  Description: Verifies the account login by using token.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code uid} — The account's id.</li>
     *          <li>{@code token} — The account's session token</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *        </ul>
     */
    @PostMapping(value = "verify")
    public ResponseEntity<Response<?>> SendMdkVerify(HttpServletRequest request, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldVerifyModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldVerifyModel.class);
            if(body == null || body.token == null || body.token.isBlank() || body.uid == null || body.uid.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.uid));
            if(myAccount == null || !myAccount.getSessionToken().equals(body.token) || myAccount.getRequireRealPerson() || myAccount.getRequireDeviceGrant() || myAccount.getRequireSafeMobile() || myAccount.getEmailBindTicket() != null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_INVALID_ACCOUNT_LOGIN_STATUS, "登录态失效，请重新登录"));
            }

            if(myAccount.getDeviceInfo().get(device_id) == null || myAccount.getIsPendingDeletion() || myAccount.getRequireAccountReactivation()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_LOGIN_NEW_LOCATION_FOUND, "请重新登录"));
            }

            myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
            myAccount.save(false);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShieldLoginResponse(myAccount, ipAddress)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/verifyEmailCaptcha">https://devapi-takumi.mihoyo.com/mdk/shield/api/verifyEmailCaptcha</a><br><br>
     *  Description: Verifies the code (captcha) that was sent by <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptchaByActionTicket">https://devapi-takumi.mihoyo.com/mdk/shield/api/emailCaptchaByActionTicket</a>.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code action_type} — The client's action type.</li>
     *          <li>{@code captcha} — The verification code.</li>
     *        </ul>
     */
    @PostMapping(value = {"verifyEmailCaptcha", "verifyMobileCaptcha"})
    public ResponseEntity<Response<?>> SendMdkVerifyEmailCaptcha(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        ShieldVerifyEmailCaptchaModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ShieldVerifyEmailCaptchaModel.class);
            if(body == null || body.action_ticket == null || body.action_ticket.isBlank() || body.action_type == null || body.captcha == null || body.captcha.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            if(!myTicket.getVerCode().equals(body.captcha)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "验证码错误"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}