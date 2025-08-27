package org.genshinhttpsrv.controllers.account;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.controllers.combo.Guard;
import org.genshinhttpsrv.database.DBUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "account/ma-cn-session", produces = "application/json")
public final class Session implements Response {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/ma-cn-session/app/logout">https://devapi-takumi.mihoyo.com/account/ma-cn-session/app/logout</a><br><br>
     *  Description: Logs out the user from their current session.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code aid} — The client's account id.</li>
     *          <li>{@code token} — The client's token id and type.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "app/logout")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogout(@RequestBody LogoutModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(Application.getPropertiesInfo().enable_heartbeat) {
            Guard.getIpStartTimes().remove(request.getRemoteAddr());
            Guard.getIpStartTimes2().remove(request.getRemoteAddr());
        }

        if(body.token == null || !body.token.has("token") || !body.token.has("token_type") || body.token.get("token_type").asInt() != 1 || body.aid == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        try {
            var myAccount = DBUtils.findAccountByToken(body.token.get("token").asText());
            if(!myAccount.getGameToken().equals(body.token.get("token").asText()) || myAccount.get_id() != Integer.parseInt(body.aid)) {
                return ResponseEntity.ok(this.makeResponse(Retcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
            }
            myAccount.setLastDisconnectionDate(System.currentTimeMillis());
            myAccount.save();
        } catch (NumberFormatException ignored) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", null));
    }


    // Classes
    public static class LogoutModel {
        public String aid;
        public JsonNode token;
    }
}