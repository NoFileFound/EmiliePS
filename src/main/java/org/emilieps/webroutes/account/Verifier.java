package org.emilieps.webroutes.account;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.data.HttpRetcode;
import org.emilieps.data.webserver.Response;
import org.emilieps.database.Ticket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.MongodbLib;

@RestController
@RequestMapping(value = {"hk4e_global/account/ma-verifier/verifier", "account/ma-verifier", "account/ma-cn-verifier"}, produces = "application/json")
public final class Verifier implements Response {
    /**
     *  Source: <a href="https://passport-api.mihoyo.com/account/ma-cn-verifier/verifier/createLoginCaptcha">https://passport-api.mihoyo.com/account/ma-cn-verifier/verifier/createLoginCaptcha</a><br><br>
     *  Description: Generates a captcha for login using a mobile number.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code area_code} — The client's mobile number area.</li>
     *          <li>{@code mobile} — The client's mobile number.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "verifier/createLoginCaptcha")
    public ResponseEntity<LinkedHashMap<String, Object>> SendCreateLoginCaptcha(@RequestBody CreateLoginCaptchaModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.mobile == null || body.area_code == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(body.is_crypto) {
            body.area_code = EncryptionLib.decryptPassword(body.area_code);
            body.mobile = EncryptionLib.decryptPassword(body.mobile);
            if(body.area_code.isEmpty() || body.mobile.isEmpty()) {
                Application.getLogger().error(Application.getTranslations().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "mdk/shield/login"));
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
            }
        }

        if(!(body.area_code + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_mobile_number_invalid"), null));
        }

        var myAccount = MongodbLib.findAccountByMobile(body.mobile, body.area_code.replace("+", ""));
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslations().get(lang, "retcode_login_account_not_found"), null));
        }

        if(MongodbLib.findTicketByAccountId(myAccount.get_id(), "loginMobile") != null) {
            MongodbLib.findTicketByAccountId(myAccount.get_id(), "loginMobile").delete();
        }

        String code = EncryptionLib.generateVerificationCode();
        var myTicket = new Ticket(myAccount.get_id(), "loginMobile");
        myTicket.setVerificationCode(code);
        myTicket.save();

        /// TODO: Send SMS.
        Application.getLogger().info(Application.getTranslations().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getMobileNumber(), code, "loginCaptcha"));
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("sent_new", true);
            put("action_type", "login_by_mobile_captcha");
            put("countdown", 100);
        }}));
    }


    // Classes
    public static class CreateLoginCaptchaModel {
        public String mobile;
        public String area_code;
        public Boolean is_crypto = true;
    }
}