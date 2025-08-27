package org.genshinhttpsrv.controllers.account;

// Imports
import static org.genshinhttpsrv.libraries.StringUtils.filterString;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.api.enums.ClientType;
import org.genshinhttpsrv.api.enums.RealNameOperation;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.database.collections.Account;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/account/ma-passport", "account/ma-passport", "account/ma-cn-passport"}, produces = "application/json")
public final class Passport implements Response {
    /**
     *  Source: <a href="https://hk4e-sdk-os-static.hoyoverse.com/hk4e_global/account/ma-passport/api/getSwitchStatus">https://hk4e-sdk-os-static.hoyoverse.com/hk4e_global/account/ma-passport/api/getSwitchStatus</a><br><br>
     *  Description: Gets the client's config for android devices.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code platform} — The client's platform type.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping(value = "api/getSwitchStatus")
    public ResponseEntity<LinkedHashMap<String, Object>> SendSwitchStatus(String app_id, ClientType platform, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(!Application.getPropertiesInfo().app_ids.isEmpty() && !Application.getPropertiesInfo().app_ids.contains(app_id)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(platform != ClientType.PLATFORM_ANDROID && platform != ClientType.PLATFORM_IOS && platform != ClientType.PLATFORM_ANDROIDCLOUD && platform != ClientType.PLATFORM_IOSCLOUD) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK",
            Map.of("switch_status_map", new LinkedHashMap<String, Object>() {{
                var props = Application.getPropertiesInfo().switch_status_map;

                if (platform == ClientType.PLATFORM_ANDROID || platform == ClientType.PLATFORM_ANDROIDCLOUD) {
                    put("ui_v2", new LinkedHashMap<String, Object>() {{
                        put("enabled", props.enable_ui_v2);
                        put("disabled_versions", props.ui_v2_disabled_versions);
                    }});
                }

                put("apple_login", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_apple_login);
                    put("disabled_versions", props.apple_login_disabled_versions);
                }});
                put("password_reset_entry", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_password_reset_entry);
                    put("disabled_versions", props.password_reset_entry_disabled_versions);
                }});
                put("vn_real_name_v2", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_vn_real_name_v2);
                    put("disabled_versions", props.vn_real_name_v2_disabled_versions);
                }});
                put("common_question_entry", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_common_question_entry);
                    put("disabled_versions", props.common_question_entry_disabled_versions);
                }});
                put("pwd_login_tab", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_pwd_login_tab);
                    put("disabled_versions", props.pwd_login_tab_disabled_versions);
                }});
                put("vn_real_name", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_vn_real_name);
                    put("disabled_versions", props.vn_real_name_disabled_versions);
                }});
                put("firebase_return_unmasked_email", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_firebase_return_unmasked_email);
                    put("disabled_versions", props.firebase_return_unmasked_email_disabled_versions);
                }});
                put("google_login", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_google_login);
                    put("disabled_versions", props.google_login_disabled_versions);
                }});
                put("bind_user_thirdparty_email", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_bind_user_thirdparty_email);
                    put("disabled_versions", props.bind_user_thirdparty_email_disabled_versions);
                }});
                put("third_party_bind_email", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_third_party_bind_email);
                    put("disabled_versions", props.third_party_bind_email_disabled_versions);
                }});
                put("bind_thirdparty", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_bind_thirdparty);
                    put("disabled_versions", props.bind_thirdparty_disabled_versions);
                }});
                put("user_name_bind_email", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_user_name_bind_email);
                    put("disabled_versions", props.user_name_bind_email_disabled_versions);
                }});
                put("account_register_tab", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_account_register_tab);
                    put("disabled_versions", props.account_register_tab_disabled_versions);
                }});
                put("twitter_login", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_twitter_login);
                    put("disabled_versions", props.twitter_login_disabled_versions);
                }});
                put("facebook_login", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_facebook_login);
                    put("disabled_versions", props.facebook_login_disabled_versions);
                }});
                put("marketing_authorization", new LinkedHashMap<String, Object>() {{
                    put("enabled", props.enable_marketing_authorization);
                    put("disabled_versions", props.marketing_authorization_disabled_versions);
                }});
            }})
        ));
    }

    /**
     *  Source: <a href="https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByMobileCaptcha">https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByMobileCaptcha</a><br><br>
     *  Description: Logins in the game using mobile.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_type} — The action name (It is always Login).</li>
     *          <li>{@code area_code} — The client's mobile number area.</li>
     *          <li>{@code mobile} — The client's mobile number.</li>
     *          <li>{@code captcha} — The verification code.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "app/loginByMobileCaptcha")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoginByMobileCaptcha(@RequestBody LoginByMobileCaptchaModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action_type == null || !body.action_type.equals("login_by_mobile_captcha") || body.area_code == null || body.area_code.isEmpty() || body.mobile == null || body.mobile.isEmpty() || body.captcha == null || body.captcha.isEmpty() || device_id == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ILLEGAL_PARAMETER, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(body.is_crypto) {
            body.area_code = EncryptionManager.decryptPassword(body.area_code);
            body.mobile = EncryptionManager.decryptPassword(body.mobile);
            if(body.area_code.isEmpty() || body.mobile.isEmpty()) {
                Application.getLogger().error(Application.getTranslationManager().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "mdk/shield/login"));
                return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
            }
        }

        var myAccount = DBUtils.findAccountByMobile(body.mobile, body.area_code.replace("+", ""));
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        var myTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), "loginMobile");
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_TICKET_NOT_EXIST, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.captcha)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_CAPTCHA_MISMATCH, Application.getTranslationManager().get(lang, "retcode_verification_code_invalid"), null));
        }

        myTicket.delete();
        if(!myAccount.getApprovedDevices().contains(device_id)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ACCOUNT_NEW_DEVICE_DETECTED, Application.getTranslationManager().get(lang, "retcode_new_device_detected"), null));
        }

        String _actionType = "";
        for (String action : new String[]{"bind_realname", "modify_realname", "bind_realperson", "verify_realperson"}) {
            var _realPersonActionTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), action);
            if (_realPersonActionTicket != null) {
                _actionType = action;
                break;
            }
        }

        myAccount.setIpAddress(request.getRemoteAddr());
        myAccount.save();
        DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
        String final_actionType = _actionType;
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("login_ticket", "");
            put("need_realperson", myAccount.getRealNameOperation() == RealNameOperation.BindRealperson || myAccount.getRealNameOperation() == RealNameOperation.VerifyRealperson);
            put("reactivate_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireReactivation());
                put("ticket", (myAccount.getIsRequireReactivation()) ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : "");
            }});
            put("realname_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireRealname());
                put("action_ticket", (myAccount.getIsRequireRealname() ? DBUtils.findTicketByAccountId(myAccount.get_id(), final_actionType).getId() : ""));
                put("action_type", final_actionType);
            }});
            put("token", new LinkedHashMap<String, Object>() {{
                put("token", myAccount.generateGameToken());
                put("token_version", 1);
            }});
            put("user_info", new LinkedHashMap<String, Object>() {{
                put("account_name", filterString(myAccount.getName()));
                put("aid", myAccount.get_id());
                put("area_code", filterString(myAccount.getMobileNumberArea()));
                put("country", myAccount.getCountryCode());
                put("email", filterString(myAccount.getEmailAddress()));
                put("identity_code", filterString(myAccount.getIdentityCard()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? 1 : 0);
                put("links", new ArrayList<>());
                put("mid", "");
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("realname", filterString(myAccount.getRealname()));
                put("rebind_area_code", "");
                put("rebind_mobile", "");
                put("rebind_mobile_time", "0");
                put("safe_area_code", filterString(myAccount.getMobileNumberArea()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("password_time", "0");
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
            }});
        }}));
    }

    /**
     *  Source: <a href="https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByPassword">https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByPassword</a><br><br>
     *  Description: Logins in the game using password.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code account} — The account name.</li>
     *          <li>{@code password} — The account's password.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "app/loginByPassword")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLoginByPassword(@RequestBody LoginByPasswordModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.account == null || body.password == null || device_id == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ILLEGAL_PARAMETER, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(body.is_crypto != null && body.is_crypto) {
            body.account = EncryptionManager.decryptPassword(body.account);
            body.password = EncryptionManager.decryptPassword(body.password);
            if(body.password.isEmpty() || body.account.isEmpty()) {
                Application.getLogger().error(Application.getTranslationManager().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "app/loginByPassword"));
                return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ILLEGAL_PARAMETER, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
            }
        }

        if(!body.account.matches("^(?:[A-Za-z0-9][A-Za-z0-9._]{2,19}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ACCOUNT_FORMAT_ERROR, Application.getTranslationManager().get(lang, "retcode_account_format_error"), null));
        }

        if(!body.password.matches("^[!-~]{8,32}$")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_PASSWORD_FORMAT_ERROR, Application.getTranslationManager().get(lang, "retcode_password_format_invalid"), null));
        }

        Account myAccount;
        if(body.account.contains("@")) {
            myAccount = DBUtils.findAccountByEmail(body.account);
        } else {
            myAccount = DBUtils.findAccountByName(body.account);
        }

        if(myAccount == null || !myAccount.getPassword().equals(EncryptionManager.md5Encode(body.password))) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ACCOUNT_NOT_EXIST, Application.getTranslationManager().get(lang, "retcode_login_account_not_found"), null));
        }

        if(!myAccount.getApprovedDevices().contains(device_id)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ACCOUNT_NEW_DEVICE_DETECTED, Application.getTranslationManager().get(lang, "retcode_new_device_detected"), null));
        }

        String _actionType = "";
        for (String action : new String[]{"bind_realname", "modify_realname", "bind_realperson", "verify_realperson"}) {
            var _realPersonActionTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), action);
            if (_realPersonActionTicket != null) {
                _actionType = action;
                break;
            }
        }

        myAccount.setIpAddress(request.getRemoteAddr());
        myAccount.save();
        DBUtils.getCachedAccountDevices().putIfAbsent(device_id, myAccount);
        String final_actionType = _actionType;
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("login_ticket", "");
            put("need_realperson", myAccount.getRealNameOperation() == RealNameOperation.BindRealperson || myAccount.getRealNameOperation() == RealNameOperation.VerifyRealperson);
            put("reactivate_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireReactivation());
                put("ticket", (myAccount.getIsRequireReactivation()) ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : "");
            }});
            put("realname_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireRealname());
                put("action_ticket", (myAccount.getIsRequireRealname() ? DBUtils.findTicketByAccountId(myAccount.get_id(), final_actionType).getId() : ""));
                put("action_type", final_actionType);
            }});
            put("token", new LinkedHashMap<String, Object>() {{
                put("token", myAccount.generateGameToken());
                put("token_version", 1);
            }});
            put("user_info", new LinkedHashMap<String, Object>() {{
                put("account_name", filterString(myAccount.getName()));
                put("aid", myAccount.get_id());
                put("area_code", filterString(myAccount.getMobileNumberArea()));
                put("country", myAccount.getCountryCode());
                put("email", filterString(myAccount.getEmailAddress()));
                put("identity_code", filterString(myAccount.getIdentityCard()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? 1 : 0);
                put("links", new ArrayList<>());
                put("mid", "");
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("realname", filterString(myAccount.getRealname()));
                put("rebind_area_code", "");
                put("rebind_mobile", "");
                put("rebind_mobile_time", "0");
                put("safe_area_code", filterString(myAccount.getMobileNumberArea()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("password_time", "0");
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
            }});
        }}));
    }

    /**
     *  Source: <a href="https://passport-api.mihoyo.com/account/ma-cn-passport/app/reactivateAccount">https://passport-api.mihoyo.com/account/ma-cn-passport/app/reactivateAccount</a><br><br>
     *  Description: Reactivates the client's account<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-device_id} — The client's device id.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "app/reactivateAccount")
    public ResponseEntity<LinkedHashMap<String, Object>> SendReactivateAccount(@RequestBody ReactivateAccountModel body, @RequestHeader(value = "x-rpc-device_id", required = false) String device_id, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.action_ticket == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ILLEGAL_PARAMETER, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_TICKET_NOT_EXIST, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        myAccount.setIsRequireReactivation(false);
        myAccount.save();
        myTicket.delete();

        if(!myAccount.getApprovedDevices().contains(device_id)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MA_PASSPORT_ACCOUNT_NEW_DEVICE_DETECTED, Application.getTranslationManager().get(lang, "retcode_new_device_detected"), null));
        }

        String _actionType = "";
        for (String action : new String[]{"bind_realname", "modify_realname", "bind_realperson", "verify_realperson"}) {
            var _realPersonActionTicket = DBUtils.findTicketByAccountId(myAccount.get_id(), action);
            if (_realPersonActionTicket != null) {
                _actionType = action;
                break;
            }
        }

        String final_actionType = _actionType;
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("login_ticket", "");
            put("need_realperson", myAccount.getRealNameOperation() == RealNameOperation.BindRealperson || myAccount.getRealNameOperation() == RealNameOperation.VerifyRealperson);
            put("reactivate_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireReactivation());
                put("ticket", (myAccount.getIsRequireReactivation()) ? DBUtils.findTicketByAccountId(myAccount.get_id(), "reactivation").getId() : "");
            }});
            put("realname_info", new LinkedHashMap<String, Object>() {{
                put("required", myAccount.getIsRequireRealname());
                put("action_ticket", (myAccount.getIsRequireRealname() ? DBUtils.findTicketByAccountId(myAccount.get_id(), final_actionType).getId() : ""));
                put("action_type", final_actionType);
            }});
            put("token", new LinkedHashMap<String, Object>() {{
                put("token", myAccount.generateGameToken());
                put("token_version", 1);
            }});
            put("user_info", new LinkedHashMap<String, Object>() {{
                put("account_name", filterString(myAccount.getName()));
                put("aid", myAccount.get_id());
                put("area_code", filterString(myAccount.getMobileNumberArea()));
                put("country", myAccount.getCountryCode());
                put("email", filterString(myAccount.getEmailAddress()));
                put("identity_code", filterString(myAccount.getIdentityCard()));
                put("is_email_verify", myAccount.getIsEmailVerified() ? 1 : 0);
                put("links", new ArrayList<>());
                put("mid", "");
                put("mobile", filterString(myAccount.getMobileNumber()));
                put("realname", filterString(myAccount.getRealname()));
                put("rebind_area_code", "");
                put("rebind_mobile", "");
                put("rebind_mobile_time", "0");
                put("safe_area_code", filterString(myAccount.getMobileNumberArea()));
                put("safe_mobile", filterString(myAccount.getSafeMobileNumber()));
                put("password_time", "0");
                put("unmasked_email", myAccount.getEmailAddress());
                put("unmasked_email_type", 1);
            }});
        }}));
    }


    // Classes
    public static class LoginByMobileCaptchaModel {
        public String action_type;
        public String mobile;
        public String captcha;
        public String area_code;
        public Boolean is_crypto = true;
    }

    public static class LoginByPasswordModel {
        public String account;
        public String password;
        public Boolean is_crypto = true;
    }

    public static class ReactivateAccountModel {
        public String action_ticket;
    }
}