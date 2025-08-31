package org.emilieps.bootspring.routes.mdk;

// Imports
import static org.emilieps.libraries.StringUtils.filterString;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.emilieps.Application;
import org.emilieps.bootspring.data.HttpRetcode;
import org.emilieps.bootspring.data.Response;
import org.emilieps.bootspring.data.enums.SceneType;
import org.emilieps.data.enums.ClientType;
import org.emilieps.data.enums.RegionType;
import org.emilieps.database.DBUtils;
import org.emilieps.database.collections.Account;
import org.emilieps.database.collections.Ticket;
import org.emilieps.libraries.EncryptionManager;
import org.emilieps.libraries.GeetestLib;
import org.emilieps.libraries.JsonLoader;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/shield/api", "hk4e_cn/mdk/shield/api", "mdk/shield/api", "takumi/hk4e_cn/mdk/shield/api", "takumi/hk4e_global/mdk/shield/api", "takumi/mdk/shield/api"}, produces = "application/json")
public final class Shield implements Response {
    private final String[] ACTIONS = {"bind_mobile", "bind_realname", "modify_realname", "bind_email"};

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
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "actionTicket")
    public ResponseEntity<LinkedHashMap<String, Object>> SendActionTicket(@RequestBody ActionTicketModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.account_id == null || body.account_id.isEmpty() || body.game_token == null || body.game_token.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        try {
            long myAccountId = Long.parseLong(body.account_id);
            var myAccount = DBUtils.findAccountById(myAccountId);
            if(myAccount == null || !myAccount.getGameToken().equals(body.game_token)) {
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
            }

            var myTicket = DBUtils.findTicketByAccountId(myAccountId, body.action_type);
            if(myTicket == null) {
                myTicket = new Ticket(myAccountId, body.action_type);
                myTicket.save();
            }

            String ticketId = myTicket.getId();
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("ticket", ticketId);
            }}));
        } catch (Exception ignored) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
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
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "bindEmail")
    public ResponseEntity<LinkedHashMap<String, Object>> SendBindEmail(@RequestBody BindEmail body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.email == null || body.email.isEmpty() || body.action_ticket == null || body.action_ticket.isEmpty() || body.captcha == null || body.captcha.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!body.email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_email_address_invalid"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null || !myTicket.getType().equals("bind_email")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.captcha)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_verification_code_invalid"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        myTicket.delete();
        myAccount.setEmailAddress(body.email);
        myAccount.setIsEmailVerified(true);
        myAccount.save();

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "emailCaptcha")
    public ResponseEntity<LinkedHashMap<String, Object>> SendEmailCaptcha(@RequestBody EmailCaptchaModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.email == null || body.email.isEmpty() || body.action_type == null || body.action_type.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(!body.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_email_address_invalid"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null || myAccount.getEmailAddress().isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        String code = EncryptionManager.generateVerificationCode();
        Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_email", myAccount.getEmailAddress(), code, body.action_type));

        /// TODO: Send email.
        myTicket.setVerificationCode(code);
        myTicket.save();

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "emailCaptchaByActionTicket")
    public ResponseEntity<LinkedHashMap<String, Object>> SendEmailCaptchaByActionTicket(@RequestBody EmailCaptchaByActionTicketModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action_ticket == null || body.action_ticket.isEmpty() || body.action_type == null || body.action_type.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null || myAccount.getEmailAddress().isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        String code = EncryptionManager.generateVerificationCode();
        Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_email", myAccount.getEmailAddress(), code, body.action_type));

        /// TODO: Send email.
        myTicket.setVerificationCode(code);
        myTicket.save();

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadConfig">https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadConfig</a><br><br>
     *  Description: Fetches configuration about the login page and region.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code game_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code package_name} — The application's package name. (For android)</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping(value = "loadConfig")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoadConfig(ClientType client, RegionType game_key, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(game_key == null || game_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(client == null || client == ClientType.PLATFORM_UNKNOWN || client == ClientType.PLATFORM_WEB || client == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_configuration_error"), null));
        }

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("id", (client == ClientType.PLATFORM_IOS ? 4 : client == ClientType.PLATFORM_ANDROID ? 5 : client == ClientType.PLATFORM_PC ? 6 : client == ClientType.PLATFORM_PS4 ? 30 : client == ClientType.PLATFORM_ANDROIDCLOUD ? 27 : client == ClientType.PLATFORM_PCCLOUD ? 53 : client == ClientType.PLATFORM_IOSCLOUD ? 26 : client == ClientType.PLATFORM_PS5 ? 28 : client == ClientType.PLATFORM_MACOSCLOUD ? 44 : client == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? 150 : client == ClientType.PLATFORM_DOUYIN_ANDROIDCLOUD ? 151 : 117));
        data.put("game_key", game_key);
        data.put("client", (client == ClientType.PLATFORM_IOS ? "IOS" : client == ClientType.PLATFORM_ANDROID ? "Android" : client == ClientType.PLATFORM_PC ? "PC" : client == ClientType.PLATFORM_PS4 ? "PS" : client == ClientType.PLATFORM_ANDROIDCLOUD ? "CloudAndroid" : client == ClientType.PLATFORM_PCCLOUD ? "CloudPC" : client == ClientType.PLATFORM_IOSCLOUD ? "CloudIOS" : client == ClientType.PLATFORM_PS5 ? "PS5" : client == ClientType.PLATFORM_MACOSCLOUD ? "CloudMacOS" : client == ClientType.PLATFORM_DOUYIN_IOSCLOUD ? "CloudDouyiniOS" : client == ClientType.PLATFORM_DOUYIN_ANDROIDCLOUD ? "CloudDouyinAndroid" : client == ClientType.PLATFORM_CX ? "CX" : client == ClientType.PLATFORM_HARMONYOSNEXT ? "HarmonyOSNEXT" : ""));
        data.put("identity", "I_IDENTITY");
        data.put("guest", Application.getPropertiesInfo().enable_guest_login);
        data.put("ignore_versions", Application.getPropertiesInfo().ignore_versions);
        data.put("scene", (client == ClientType.PLATFORM_PC && game_key == RegionType.REGION_CHINA ? SceneType.S_ACCOUNT : SceneType.S_NORMAL));
        data.put("name", (game_key == RegionType.REGION_CHINA ? "原神" : "原神海外"));
        data.put("disable_regist", Application.getPropertiesInfo().disable_regist);
        data.put("enable_email_captcha", !Application.getPropertiesInfo().disable_mmt);
        data.put("thirdparty", Application.getPropertiesInfo().thirdparty);
        data.put("disable_mmt", Application.getPropertiesInfo().disable_mmt);
        data.put("server_guest", Application.getPropertiesInfo().server_guest);
        data.put("thirdparty_ignore", Application.getPropertiesInfo().thirdparty_ignore);
        data.put("enable_ps_bind_account", Application.getPropertiesInfo().enable_ps_bind_account);
        data.put("thirdparty_login_configs", Application.getPropertiesInfo().thirdparty_login_configs);
        data.put("initialize_firebase", Application.getPropertiesInfo().initialize_firebase);
        data.put("bbs_auth_login", Application.getPropertiesInfo().bbs_auth_login);
        data.put("bbs_auth_login_ignore", Application.getPropertiesInfo().bbs_auth_login_ignore);
        data.put("fetch_instance_id", Application.getPropertiesInfo().fetch_instance_id);
        data.put("enable_flash_login", Application.getPropertiesInfo().enable_flash_login);
        data.put("enable_logo_18", Application.getPropertiesInfo().logo_eighteenplus.enabled);
        data.put("logo_height", Application.getPropertiesInfo().logo_eighteenplus.logo_height);
        data.put("logo_width", Application.getPropertiesInfo().logo_eighteenplus.logo_width);
        data.put("enable_cx_bind_account", Application.getPropertiesInfo().enable_cx_bind_account);
        data.put("firebase_blacklist_devices_switch", Application.getPropertiesInfo().firebase_blacklist_devices_switch);
        data.put("firebase_blacklist_devices_version", Application.getPropertiesInfo().firebase_blacklist_devices_version);
        data.put("hoyolab_auth_login", Application.getPropertiesInfo().hoyolab_auth_login);
        data.put("hoyolab_auth_login_ignore", Application.getPropertiesInfo().hoyolab_auth_login_ignore);
        data.put("hoyoplay_auth_login", Application.getPropertiesInfo().hoyoplay_auth_login);
        if(game_key == RegionType.REGION_CHINA) {
            data.put("enable_douyin_flash_login", Application.getPropertiesInfo().enable_douyin_flash_login);
            data.put("enable_age_gate", Application.getPropertiesInfo().enable_age_gate);
            data.put("enable_age_gate_ignore", Application.getPropertiesInfo().enable_age_gate_ignore);
        }
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", data));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadFirebaseBlackList">https://devapi-static.mihoyo.com/takumi/mdk/shield/api/loadFirebaseBlackList</a><br><br>
     *  Description: Fetches configuration about the login page and region.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code game_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping(value = "loadFirebaseBlackList")
    public ResponseEntity<LinkedHashMap<String, Object>> SendFirebaseBlackList(ClientType client, RegionType game_key, @RequestHeader(value = "x-rpc-language", required = false) String lang) throws JsonProcessingException {
        if(game_key == null || game_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(client == null || client == ClientType.PLATFORM_UNKNOWN || client == ClientType.PLATFORM_WEB || client == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_configuration_error"), null));
        }

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("device_blacklist_version", Application.getPropertiesInfo().firebase_blacklist_devices_version);
        data.put("device_blacklist_switch", Application.getPropertiesInfo().firebase_blacklist_devices_switch);
        data.put("device_blacklist", JsonLoader.toJson(new LinkedHashMap<>() {{
            put("min_api", 28);
            put("device", Application.getPropertiesInfo().firebase_blacklist_devices);
        }}));

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", data));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "login")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogin(@RequestBody LoginModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.account == null || body.password == null || device_id == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        if(!body.account.matches("^(?:[A-Za-z0-9][A-Za-z0-9._]{2,19}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_ACCOUNT_INVALID_FORMAT, Application.getTranslationManager().get(lang, "retcode_account_format_error"), null));
        }

        if(body.is_crypto != null && body.is_crypto) {
            body.password = EncryptionManager.decryptPassword(body.password);
            if(body.password.isEmpty()) {
                Application.getLogger().error(Application.getTranslationManager().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "mdk/shield/login"));
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
            }
        }

        if(!body.password.matches("^[!-~]{8,32}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_password_format_invalid"), null));
        }

        Account myAccount;
        if(body.account.contains("@")) {
            myAccount = DBUtils.findAccountByEmail(body.account);
        } else {
            myAccount = DBUtils.findAccountByName(body.account);
        }

        if(myAccount == null || !myAccount.getPassword().equals(EncryptionManager.md5Encode(body.password))) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslationManager().get(lang, "retcode_login_account_not_found"), null));
        }

        if(!myAccount.getApprovedDevices().contains(device_id)) {
            myAccount.setIsRequireDeviceGrant(true);
            var myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant");
            if(myTicket == null) {
                myTicket = new Ticket(myAccount.get_id(), "device_grant");
                myTicket.save();
            }
        }

        myAccount.setIpAddress(request.getRemoteAddr());
        myAccount.save();
        DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("account", new LinkedHashMap<>() {{
                put("uid", myAccount.get_id());
                put("name", myAccount.getName());
                put("email", filterString(myAccount.getEmailAddress()));
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? '1' : '0');
                put("realname", filterString(myAccount.getRealname()));
                put("identity_card", filterString(myAccount.getIdentityCard()));
                put("token", myAccount.generateGameToken());
                put("facebook_name", filterString(myAccount.getFacebookName()));
                put("google_name", filterString(myAccount.getGoogleName()));
                put("twitter_name", filterString(myAccount.getTwitterName()));
                put("game_center_name", filterString(myAccount.getGameCenterName()));
                put("apple_name", filterString(myAccount.getAppleName()));
                put("sony_name", filterString(myAccount.getSonyName()));
                put("tap_name", filterString(myAccount.getTapName()));
                put("country", myAccount.getCountryCode());
                put("reactivate_ticket", (myAccount.getIsRequireReactivation() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : ""));
                put("area_code", myAccount.getMobileNumberArea());
                put("device_grant_ticket", (myAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant").getId() : ""));
                put("steam_name", filterString(myAccount.getSteamName()));
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
                put("cx_name", filterString(myAccount.getCxName()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("age_gate_info", null);
            }});
            put("realperson_required", myAccount.getIsRequireRealname());
            put("safe_moblie_required", myAccount.getIsRequireSafeMobile());
            put("reactivate_required", myAccount.getIsRequireReactivation());
            put("device_grant_required", myAccount.getIsRequireDeviceGrant());
            put("realname_operation", myAccount.getRealNameOperation().toString());
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/loginByAuthTicket">https://devapi-takumi.mihoyo.com/mdk/shield/api/loginByAuthTicket</a><br><br>
     *  Description: Logins in the game using authorization token.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code login_type} — The login type.</li>
     *          <li>{@code action_ticket} — The authorization ticket.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "loginByAuthTicket")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogin(@RequestBody LoginByAuthTicket body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.auth_ticket == null || body.auth_ticket.isEmpty() || !body.login_type.equals("thirdparty")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = DBUtils.findTicketById(body.auth_ticket);
        if(myTicket == null || !myTicket.getType().equals("login")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        myTicket.delete();
        if(!myAccount.getApprovedDevices().contains(device_id)) {
            myAccount.setIsRequireDeviceGrant(true);
            myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant");
            if(myTicket == null) {
                myTicket = new Ticket(myAccount.get_id(), "device_grant");
                myTicket.save();
            }
        }

        myAccount.setIpAddress(request.getRemoteAddr());
        myAccount.save();
        DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("account", new LinkedHashMap<>() {{
                put("uid", myAccount.get_id());
                put("name", myAccount.getName());
                put("email", filterString(myAccount.getEmailAddress()));
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? '1' : '0');
                put("realname", filterString(myAccount.getRealname()));
                put("identity_card", filterString(myAccount.getIdentityCard()));
                put("token", myAccount.generateGameToken());
                put("facebook_name", filterString(myAccount.getFacebookName()));
                put("google_name", filterString(myAccount.getGoogleName()));
                put("twitter_name", filterString(myAccount.getTwitterName()));
                put("game_center_name", filterString(myAccount.getGameCenterName()));
                put("apple_name", filterString(myAccount.getAppleName()));
                put("sony_name", filterString(myAccount.getSonyName()));
                put("tap_name", filterString(myAccount.getTapName()));
                put("country", myAccount.getCountryCode());
                put("reactivate_ticket", (myAccount.getIsRequireReactivation() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : ""));
                put("area_code", myAccount.getMobileNumberArea());
                put("device_grant_ticket", (myAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant").getId() : ""));
                put("steam_name", filterString(myAccount.getSteamName()));
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
                put("cx_name", filterString(myAccount.getCxName()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("age_gate_info", null);
            }});
            put("realperson_required", myAccount.getIsRequireRealname());
            put("safe_moblie_required", myAccount.getIsRequireSafeMobile());
            put("reactivate_required", myAccount.getIsRequireReactivation());
            put("device_grant_required", myAccount.getIsRequireDeviceGrant());
            put("realname_operation", myAccount.getRealNameOperation().toString());
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shield/api/loginByThirdparty">https://devapi-takumi.mihoyo.com/mdk/shield/api/loginByThirdparty</a><br><br>
     *  Description: Logins in the game using third party application.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code type} — The thirdparty name.</li>
     *          <li>{@code token} — The thirdparty token.</li>
     *          <li>{@code redirect_url} — The redirect page after login.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "loginByThirdparty")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoginByThirdParty(@RequestBody LoginByThirdPartyModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.type == null || body.type.isEmpty() || body.token == null || body.token.isEmpty() || !body.redirect_url.matches("^(https?://)([\\w\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        Account myAccount = null;
        if (body.type.equals("Twitter")) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(body.token);

            var response = restTemplate.exchange("https://api.twitter.com/2/users/me?user.fields=name", HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getStatusCode() != HttpStatusCode.valueOf(200)) {
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_WRONG_ACCOUNT, Application.getTranslationManager().get(lang, "retcode_account_error"), null));
            }

            if(response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                String displayName = (String) data.get("name");
                myAccount = DBUtils.findAccountByThirdParty(displayName, "Twitter");
                if (myAccount == null) {
                    return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslationManager().get(lang, "retcode_login_account_not_found"), null));
                }
            }
        }

        if(myAccount != null) {
            if(!myAccount.getApprovedDevices().contains(device_id)) {
                myAccount.setIsRequireDeviceGrant(true);
                var myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant");
                if(myTicket == null) {
                    myTicket = new Ticket(myAccount.get_id(), "device_grant");
                    myTicket.save();
                }
            }

            myAccount.setIpAddress(request.getRemoteAddr());
            myAccount.save();
            DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
            Account finalMyAccount = myAccount;
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("account", new LinkedHashMap<>() {{
                    put("uid", finalMyAccount.get_id());
                    put("name", finalMyAccount.getName());
                    put("email", filterString(finalMyAccount.getEmailAddress()));
                    put("mobile", filterString(finalMyAccount.getMobileNumber()));
                    put("is_email_verify", finalMyAccount.getIsEmailVerified() ? '1' : '0');
                    put("realname", filterString(finalMyAccount.getRealname()));
                    put("identity_card", filterString(finalMyAccount.getIdentityCard()));
                    put("token", finalMyAccount.generateGameToken());
                    put("facebook_name", filterString(finalMyAccount.getFacebookName()));
                    put("google_name", filterString(finalMyAccount.getGoogleName()));
                    put("twitter_name", filterString(finalMyAccount.getTwitterName()));
                    put("game_center_name", filterString(finalMyAccount.getGameCenterName()));
                    put("apple_name", filterString(finalMyAccount.getAppleName()));
                    put("sony_name", filterString(finalMyAccount.getSonyName()));
                    put("tap_name", filterString(finalMyAccount.getTapName()));
                    put("country", finalMyAccount.getCountryCode());
                    put("reactivate_ticket", (finalMyAccount.getIsRequireReactivation() ? DBUtils.findTicketByAccountId(finalMyAccount.get_id(), "reactivation").getId() : ""));
                    put("area_code", finalMyAccount.getMobileNumberArea());
                    put("device_grant_ticket", (finalMyAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(finalMyAccount.get_id(), "device_grant").getId() : ""));
                    put("steam_name", filterString(finalMyAccount.getSteamName()));
                    put("unmasked_email", finalMyAccount.getEmailAddress());
                    put("unmasked_email_type", 1);
                    put("cx_name", filterString(finalMyAccount.getCxName()));
                    put("safe_mobile", filterString(finalMyAccount.getSafeMobileNumber()));
                    put("age_gate_info", null);
                }});
                put("realperson_required", finalMyAccount.getIsRequireRealname());
                put("safe_moblie_required", finalMyAccount.getIsRequireSafeMobile());
                put("reactivate_required", finalMyAccount.getIsRequireReactivation());
                put("device_grant_required", finalMyAccount.getIsRequireDeviceGrant());
                put("realname_operation", finalMyAccount.getRealNameOperation().toString());
                put("redirect_url", body.redirect_url);
            }}));
        }
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslationManager().get(lang, "retcode_login_account_not_found"), null));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "loginCaptcha")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoginCaptcha(@RequestBody LoginCaptchaModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.mobile == null || body.area == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        if(!(body.area + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_mobile_number_invalid"), null));
        }

        var myAccount = DBUtils.findAccountByMobile(body.mobile, body.area.replace("+", ""));
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslationManager().get(lang, "retcode_login_account_not_found"), null));
        }

        if(DBUtils.findTicketByAccountId(myAccount.get_id(), "loginMobile") != null) {
            DBUtils.findTicketByAccountId(myAccount.get_id(), "loginMobile").delete();
        }

        String code = EncryptionManager.generateVerificationCode();
        var myTicket = new Ticket(myAccount.get_id(), "loginMobile");
        myTicket.setVerificationCode(code);
        myTicket.save();

        /// TODO: Send SMS.
        Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getMobileNumber(), code, "loginCaptcha"));
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("action", "Login");
        }}));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "loginMobile")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoginMobile(@RequestBody LoginMobileModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action == null || !body.action.equals("Login") || body.area == null || body.area.isEmpty() || body.mobile == null || body.mobile.isEmpty() || body.captcha == null || body.captcha.isEmpty() || device_id == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myAccount = DBUtils.findAccountByMobile(body.mobile, body.area.replace("+", ""));
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        var myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "loginMobile");
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.captcha)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_verification_code_invalid"), null));
        }

        myTicket.delete();
        if(!myAccount.getApprovedDevices().contains(device_id)) {
            myAccount.setIsRequireDeviceGrant(true);
            myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant");
            if(myTicket == null) {
                myTicket = new Ticket(myAccount.get_id(), "device_grant");
                myTicket.save();
            }
        }

        myAccount.setIpAddress(request.getRemoteAddr());
        myAccount.save();
        DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("account", new LinkedHashMap<>() {{
                put("uid", myAccount.get_id());
                put("name", myAccount.getName());
                put("email", filterString(myAccount.getEmailAddress()));
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? '1' : '0');
                put("realname", filterString(myAccount.getRealname()));
                put("identity_card", filterString(myAccount.getIdentityCard()));
                put("token", myAccount.generateGameToken());
                put("facebook_name", filterString(myAccount.getFacebookName()));
                put("google_name", filterString(myAccount.getGoogleName()));
                put("twitter_name", filterString(myAccount.getTwitterName()));
                put("game_center_name", filterString(myAccount.getGameCenterName()));
                put("apple_name", filterString(myAccount.getAppleName()));
                put("sony_name", filterString(myAccount.getSonyName()));
                put("tap_name", filterString(myAccount.getTapName()));
                put("country", myAccount.getCountryCode());
                put("reactivate_ticket", (myAccount.getIsRequireReactivation() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : ""));
                put("area_code", myAccount.getMobileNumberArea());
                put("device_grant_ticket", (myAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant").getId() : ""));
                put("steam_name", filterString(myAccount.getSteamName()));
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
                put("cx_name", filterString(myAccount.getCxName()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("age_gate_info", null);
            }});
            put("realperson_required", myAccount.getIsRequireRealname());
            put("safe_moblie_required", myAccount.getIsRequireSafeMobile());
            put("reactivate_required", myAccount.getIsRequireReactivation());
            put("device_grant_required", myAccount.getIsRequireDeviceGrant());
            put("realname_operation", myAccount.getRealNameOperation().toString());
        }}));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "mobileCaptcha")
    public ResponseEntity<LinkedHashMap<String, Object>> SendMobileCaptcha(@RequestBody MobileCaptchaModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action_ticket == null || body.action_ticket.isEmpty() || body.action_type == null || body.action_type.isEmpty() || body.mobile == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(!(body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_mobile_number_invalid"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        if(body.mobile.isEmpty()) {
            body.mobile = myAccount.getMobileNumber();
        }

        String code = EncryptionManager.generateVerificationCode();
        myTicket.setVerificationCode(code);
        myTicket.getParams().put("safe_mobile", body.safe_mobile);
        myTicket.save();

        /// TODO: Send SMS.
        Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", body.mobile, code, body.action_type));
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
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
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "reactivateAccount")
    public ResponseEntity<LinkedHashMap<String, Object>> SendReactivateAccount(@RequestBody ReactivateAccountModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.action_ticket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        myAccount.setIsRequireReactivation(false);
        myAccount.save();

        myTicket.delete();
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("account", new LinkedHashMap<>() {{
                put("uid", myAccount.get_id());
                put("name", "");
                put("email", filterString(myAccount.getEmailAddress()));
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? '1' : '0');
                put("realname", filterString(myAccount.getRealname()));
                put("identity_card", filterString(myAccount.getIdentityCard()));
                put("token", myAccount.generateGameToken());
                put("facebook_name", filterString(myAccount.getFacebookName()));
                put("google_name", filterString(myAccount.getGoogleName()));
                put("twitter_name", filterString(myAccount.getTwitterName()));
                put("game_center_name", filterString(myAccount.getGameCenterName()));
                put("apple_name", filterString(myAccount.getAppleName()));
                put("sony_name", filterString(myAccount.getSonyName()));
                put("tap_name", filterString(myAccount.getTapName()));
                put("country", myAccount.getCountryCode());
                put("area_code", myAccount.getMobileNumberArea());
                put("device_grant_ticket", (myAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant").getId() : ""));
                put("steam_name", filterString(myAccount.getSteamName()));
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
                put("cx_name", filterString(myAccount.getCxName()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("age_gate_info", null);
            }});
            put("realperson_required", myAccount.getIsRequireRealname());
            put("safe_moblie_required", myAccount.getIsRequireSafeMobile());
            put("device_grant_required", myAccount.getIsRequireDeviceGrant());
            put("realname_operation", myAccount.getRealNameOperation().toString());
        }}));
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
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "verify")
    public ResponseEntity<LinkedHashMap<String, Object>> SendVerify(@RequestBody VerifyModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.uid == null || body.uid.isEmpty() || body.token == null || body.token.isEmpty() || device_id == null || device_id.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myAccount = DBUtils.findAccountById(Long.valueOf(body.uid));
        if(myAccount == null || !myAccount.getGameToken().equals(body.token)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_STATUS, Application.getTranslationManager().get(lang, "retcode_invalid_login_status"), null));
        }

        if(!myAccount.getApprovedDevices().contains(device_id)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NEW_LOCATION_FOUND, Application.getTranslationManager().get(lang, "retcode_login_new_location_found"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("account", new LinkedHashMap<>() {{
                put("uid", myAccount.get_id());
                put("name", "");
                put("email", filterString(myAccount.getEmailAddress()));
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? '1' : '0');
                put("realname", filterString(myAccount.getRealname()));
                put("identity_card", filterString(myAccount.getIdentityCard()));
                put("token", myAccount.generateGameToken());
                put("facebook_name", filterString(myAccount.getFacebookName()));
                put("google_name", filterString(myAccount.getGoogleName()));
                put("twitter_name", filterString(myAccount.getTwitterName()));
                put("game_center_name", filterString(myAccount.getGameCenterName()));
                put("apple_name", filterString(myAccount.getAppleName()));
                put("sony_name", filterString(myAccount.getSonyName()));
                put("tap_name", filterString(myAccount.getTapName()));
                put("country", myAccount.getCountryCode());
                put("reactivate_ticket", (myAccount.getIsRequireReactivation() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : ""));
                put("area_code", myAccount.getMobileNumberArea());
                put("device_grant_ticket", (myAccount.getIsRequireDeviceGrant() ? DBUtils.findTicketByAccountId(myAccount.get_id(), "device_grant").getId() : ""));
                put("steam_name", filterString(myAccount.getSteamName()));
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
                put("cx_name", filterString(myAccount.getCxName()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("age_gate_info", null);
            }});
            put("realperson_required", myAccount.getIsRequireRealname());
            put("safe_moblie_required", myAccount.getIsRequireSafeMobile());
            put("reactivate_required", myAccount.getIsRequireReactivation());
            put("device_grant_required", myAccount.getIsRequireDeviceGrant());
            put("realname_operation", myAccount.getRealNameOperation().toString());
        }}));
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
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = {"verifyEmailCaptcha", "verifyMobileCaptcha"})
    public ResponseEntity<LinkedHashMap<String, Object>> SendVerifyEmailCaptcha(@RequestBody VerifyEmailCaptchaModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action_ticket == null || body.action_ticket.isEmpty() || body.action_type == null || body.action_type.isEmpty() || body.captcha == null || body.captcha.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null || !myTicket.getType().equals(body.action_type)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.captcha)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_verification_code_invalid"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
    }


    // Classes
    public static class ActionTicketModel {
        public String account_id;
        public String action_type;
        public String game_token;
    }

    public static class BindEmail {
        public String action_ticket;
        public String email;
        public String captcha;
    }

    public static class EmailCaptchaModel {
        public String email;
        public String action_type;
        public String action_ticket;
    }

    public static class EmailCaptchaByActionTicketModel {
        public String action_ticket;
        public String action_type;
    }

    public static class LoginModel {
        public String account;
        public Boolean is_crypto;
        public String password;
    }

    public static class LoginByAuthTicket {
        public String login_type;
        public String auth_ticket;
    }

    public static class LoginByThirdPartyModel {
        public String type;
        public String token;
        public String redirect_url;
    }

    public static class LoginCaptchaModel {
        public String mobile;
        public String area;
    }

    public static class LoginMobileModel {
        public String action;
        public String area;
        public String mobile;
        public String captcha;
    }

    public static class MobileCaptchaModel {
        public String action_type;
        public String action_ticket;
        public String mobile;
        public boolean safe_mobile;
    }

    public static class ReactivateAccountModel {
        public String action_ticket;
    }

    public static class VerifyModel {
        public String uid;
        public String token;
    }

    public static class VerifyEmailCaptchaModel {
        public String action_ticket;
        public String action_type;
        public String captcha;
    }
}