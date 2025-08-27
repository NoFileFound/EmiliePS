package org.genshinhttpsrv.controllers.account;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.api.enums.GrantType;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.database.collections.Ticket;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.genshinhttpsrv.libraries.GeetestLib;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "account/device/api", produces = "application/json")
public final class Device implements Response {
    private final Map<String, String> temporarilyDevicesInfo = new HashMap<>();

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/preGrantByGame">https://devapi-takumi.mihoyo.com/account/device/api/preGrantByGame</a><br><br>
     *  Description: Sends email or SMS notification about new device entrance with a code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_token} — The client's game token.</li>
     *          <li>{@code device} — Information about the client's current device. (type, name, id and model).</li>
     *          <li>{@code way} — The way of sending the notification (SMS/Email).</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "preGrantByGame")
    public ResponseEntity<LinkedHashMap<String, Object>> SendPreGrantByGame(@RequestBody PreGrantByGameModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.game_token == null || body.game_token.isEmpty() || body.device == null | body.way == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountByToken(body.game_token);
        if(myAccount == null || !myAccount.getIsRequireDeviceGrant()) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = new Ticket(myAccount.get_id(), "device_grant");
        switch (body.way) {
            case Way_Email -> {
                if(myAccount.getEmailAddress().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_email", myAccount.getEmailAddress(), code, "device_grant"));

                /// TODO: Send email.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
            case Way_BindMobile -> {
                if(myAccount.getMobileNumber().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getMobileNumber(), code, "device_grant"));

                /// TODO: Send SMS.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
            case Way_SafeMobile -> {
                if(myAccount.getSafeMobileNumber().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getSafeMobileNumber(), code, "device_grant"));

                /// TODO: Send SMS.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
        }

        this.temporarilyDevicesInfo.putIfAbsent(myTicket.getId(), body.device.toString());
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("ticket", myTicket.getId());
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/preGrantByTicket">https://devapi-takumi.mihoyo.com/account/device/api/preGrantByTicket</a><br><br>
     *  Description: Sends email or SMS notification about new device entrance with a code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code device} — Information about the client's current device. (type, name, id and model).</li>
     *          <li>{@code way} — The way of sending the notification (SMS/Email).</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "preGrantByTicket")
    public ResponseEntity<LinkedHashMap<String, Object>> SendPreGrantByTicket(@RequestBody PreGrantByTicketModel body, @RequestHeader(value = "x-rpc-risky", required = false) String risky, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.action_ticket == null || body.action_ticket.isEmpty() || body.device == null | body.way == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!GeetestLib.checkVerifiedChallenge(risky)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myTicket = DBUtils.findTicketById(body.action_ticket);
        if(myTicket == null || !myTicket.getType().equals("device_grant")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null || !myAccount.getIsRequireDeviceGrant()) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        switch (body.way) {
            case Way_Email -> {
                if(myAccount.getEmailAddress().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_email", myAccount.getEmailAddress(), code, "device_grant"));

                /// TODO: Send email.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
            case Way_BindMobile -> {
                if(myAccount.getMobileNumber().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getMobileNumber(), code, "device_grant"));

                /// TODO: Send SMS.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
            case Way_SafeMobile -> {
                if(myAccount.getSafeMobileNumber().isEmpty()) {
                    return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
                }

                String code = EncryptionManager.generateVerificationCode();
                Application.getLogger().info(Application.getTranslationManager().get("console", "new_ver_code_generated_mobile", myAccount.getMobileNumberArea() + myAccount.getSafeMobileNumber(), code, "device_grant"));

                /// TODO: Send SMS.
                myTicket.setVerificationCode(code);
                myTicket.save();
            }
        }

        this.temporarilyDevicesInfo.putIfAbsent(myTicket.getId(), body.device.toString());
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("ticket", body.action_ticket);
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/grant">https://devapi-takumi.mihoyo.com/account/device/api/grant</a><br><br>
     *  Description: Sends email or SMS notification about new device entrance with a code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code code} — The verification code.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "grant")
    public ResponseEntity<LinkedHashMap<String, Object>> SendGrant(@RequestBody GrantModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.ticket == null || body.ticket.isEmpty() || body.code == null || body.code.length() != 6) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = DBUtils.findTicketById(body.ticket);
        if(myTicket == null || !myTicket.getType().equals("device_grant")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        if(!myTicket.getVerificationCode().equals(body.code)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_verification_code_invalid"), null));
        }

        var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
        if(myAccount == null || !myAccount.getIsRequireDeviceGrant()) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        JsonNode data = JsonLoader.parseJsonSafe(this.temporarilyDevicesInfo.get(body.ticket));
        if(data == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        String gameToken = myAccount.generateGameToken();
        myAccount.getApprovedDevices().add(data.get("device_id").asText());
        myAccount.setIsRequireDeviceGrant(false);
        myAccount.save();
        myTicket.delete();

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("login_ticket", "");
            put("game_token", gameToken);
        }}));
    }


    // Classes
    public static class GrantModel {
        public String ticket;
        public String code;
    }

    public static class PreGrantByGameModel {
        public String game_token;
        public JsonNode device;
        public GrantType way;
    }

    public static class PreGrantByTicketModel {
        public String action_ticket;
        public JsonNode device;
        public GrantType way;
    }
}