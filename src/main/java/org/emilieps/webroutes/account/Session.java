package org.emilieps.webroutes.account;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.data.HttpRetcode;
import org.emilieps.data.webserver.Response;
import org.emilieps.webroutes.combo.Guard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.MongodbLib;

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
        if(Application.getHttpConfig().enable_heartbeat) {
            Guard.getIpStartTimes().remove(request.getRemoteAddr());
            Guard.getIpStartTimes2().remove(request.getRemoteAddr());
        }

        if(body.token == null || !body.token.has("token") || !body.token.has("token_type") || body.token.get("token_type").asInt() != 1 || body.aid == null) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        try {
            var myAccount = MongodbLib.findAccountByToken(body.token.get("token").asText());
            if(!myAccount.getGameToken().equals(body.token.get("token").asText()) || myAccount.get_id() != Integer.parseInt(body.aid)) {
                return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
            }
            myAccount.setLastDisconnectionDate(System.currentTimeMillis());
            myAccount.save();
        } catch (NumberFormatException ignored) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", null));
    }


    // Classes
    public static class LogoutModel {
        public String aid;
        public JsonNode token;
    }
}