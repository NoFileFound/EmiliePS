package org.emilieps.webroutes.account;

// Imports
import static org.emilieps.utils.StringUtils.filterString;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.data.HttpRetcode;
import org.emilieps.data.webserver.Response;
import org.emilieps.data.enums.webserver.RealNameOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.MongodbLib;

@RestController
@RequestMapping(value = "account/auth/api", produces = "application/json")
public final class Auth implements Response {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/auth/api/bindMobile">https://devapi-takumi.mihoyo.com/account/auth/api/bindMobile</a><br><br>
     *  Description: Binds the phone number to the account.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code area_code} — The mobile number's area code.</li>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code mobile} — The mobile number.</li>
     *          <li>{@code captcha} — The client's verification code.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = {"bindMobile", "bindSafeMobile"})
    public ResponseEntity<LinkedHashMap<String, Object>> SendBindMobile(@RequestBody BindMobileModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.area_code == null || body.area_code.isEmpty() || body.ticket == null || body.ticket.isEmpty() || body.mobile == null || body.mobile.isEmpty() || body.captcha == null || body.captcha.isEmpty() || body.uid == null || body.uid.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_AUTH_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(!(body.area_code + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_mobile_number_invalid"), null));
        }

        var myTicket = MongodbLib.findTicketById(body.ticket);
        if(myTicket == null || (!myTicket.getType().equals("bind_mobile") && !myTicket.getType().equals("bind_safemobile"))) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.captcha)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslations().get(lang, "retcode_verification_code_invalid"), null));
        }

        var myAccount = MongodbLib.findAccountById(myTicket.getAccountId());
        if(myAccount == null || !String.valueOf(myAccount.get_id()).equals(body.uid)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        myAccount.setMobileNumberArea(body.area_code.replace("+", ""));
        if((Boolean)myTicket.getParams().get("safe_mobile")) {
            myAccount.setSafeMobileNumber(body.mobile);
            myAccount.setIsRequireSafeMobile(false);
        } else {
            myAccount.setMobileNumber(body.mobile);
        }

        myTicket.delete();
        myAccount.save();

		if(Application.getApplicationConfig().is_debug) {
			Application.getLogger().info(Application.getTranslations().get("console", "bind_mobile_action_completed", myAccount.getEmailAddress()));
		}
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/auth/api/bindRealname">https://devapi-takumi.mihoyo.com/account/auth/api/bindRealname</a><br><br>
     *  Description: Binds the real name and identity card to the account.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code realname} — The real name.</li>
     *          <li>{@code identity} — The identity card.</li>
     *          <li>{@code is_crypto} — Are the real name and identity card encrypted.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "bindRealname")
    public ResponseEntity<LinkedHashMap<String, Object>> SendBindRealname(@RequestBody BindRealnameModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.ticket == null || body.ticket.isEmpty() || body.realname == null || body.realname.isEmpty() || body.identity == null || body.identity.isEmpty() || body.is_crypto == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_AUTH_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        String encRealName = body.realname;
        String encIdentityCard = body.identity;
        if(body.is_crypto) {
            body.realname = EncryptionLib.decryptIdentity(body.realname);
            body.identity = EncryptionLib.decryptIdentity(body.identity);
            if(body.realname.isEmpty() || body.identity.isEmpty()) {
                Application.getLogger().error(Application.getTranslations().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "mdk/shield/login"));
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
            }
        }

        if(!body.identity.matches("^[1-9]\\d{5}(19\\d{2}|20\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_identity_card_invalid"), null));
        }

        var myTicket = MongodbLib.findTicketById(body.ticket);
        if(myTicket == null || !myTicket.getType().equals("bind_realname")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        var myAccount = MongodbLib.findAccountById(myTicket.getAccountId());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        myAccount.setRealname(body.realname);
        myAccount.setIdentityCard(body.identity);
        myAccount.setIsRequireRealname(false);
        myAccount.setRealNameOperation(RealNameOperation.None);
        myTicket.delete();
        myAccount.save();

		if(Application.getApplicationConfig().is_debug) {
			Application.getLogger().info(Application.getTranslations().get("console", "bind_realname_action_completed", myAccount.getEmailAddress()));
		}

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("realname_operation", "completed");
            put("uid", myAccount.get_id());
            put("name", filterString(myAccount.getName()));
            put("email", filterString(myAccount.getEmailAddress()));
            put("identity_card", encIdentityCard);
            put("realname", encRealName);
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/auth/api/modifyRealname">https://devapi-takumi.mihoyo.com/account/auth/api/modifyRealname</a><br><br>
     *  Description: Modifies the real name and identity card associated with the account.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code realname} — The real name.</li>
     *          <li>{@code identity} — The identity card.</li>
     *          <li>{@code is_crypto} — Are the real name and identity card encrypted.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "modifyRealname")
    public ResponseEntity<LinkedHashMap<String, Object>> SendModifyRealname(@RequestBody ModifyRealnameModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body.action_ticket == null || body.action_ticket.isEmpty() || body.realname == null || body.realname.isEmpty() || body.identity_card == null || body.identity_card.isEmpty() || body.is_crypto == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_AUTH_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        String encRealName = body.realname;
        String encIdentityCard = body.identity_card;
        if(body.is_crypto) {
            body.realname = EncryptionLib.decryptIdentity(body.realname);
            body.identity_card = EncryptionLib.decryptIdentity(body.identity_card);
            if(body.realname.isEmpty() || body.identity_card.isEmpty()) {
                Application.getLogger().error(Application.getTranslations().get("console", "unable_to_decrypt_password", request.getRemoteAddr(), "mdk/shield/login"));
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
            }
        }

        if(!body.identity_card.matches("^[1-9]\\d{5}(19\\d{2}|20\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_identity_card_invalid"), null));
        }

        var myTicket = MongodbLib.findTicketById(body.action_ticket);
        if(myTicket == null || !myTicket.getType().equals("modify_realname")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        var myAccount = MongodbLib.findAccountById(myTicket.getAccountId());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        myAccount.setRealname(body.realname);
        myAccount.setIdentityCard(body.identity_card);
        myAccount.setIsRequireRealname(false);
        myAccount.setRealNameOperation(RealNameOperation.None);
        myTicket.delete();
        myAccount.save();

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("realname_operation", "completed");
            put("uid", myAccount.get_id());
            put("name", filterString(myAccount.getName()));
            put("email", filterString(myAccount.getEmailAddress()));
            put("identity_card", encIdentityCard);
            put("realname", encRealName);
        }}));
    }


    // Classes
    public static class BindMobileModel {
        public String area_code;
        public String ticket;
        public String mobile;
        public String captcha;
        public String uid;
    }

    public static class BindRealnameModel {
        public String ticket;
        public String realname;
        public String identity;
        public Boolean is_crypto;
    }

    public static class ModifyRealnameModel {
        public String action_ticket;
        public String realname;
        public String identity_card;
        public Boolean is_crypto;
    }
}