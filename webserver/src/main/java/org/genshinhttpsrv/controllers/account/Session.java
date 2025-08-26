package org.genshinhttpsrv.controllers.account;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.controllers.combo.Guard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "account/ma-cn-session", produces = "application/json")
public final class Session implements Response {
    /**
     *  Source: <a href="https://passport-api.mihoyo.com/account/ma-cn-session/app/logout">https://passport-api.mihoyo.com/account/ma-cn-session/app/logout</a><br><br>
     *  Description: Disconnects the client's connection.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code aid} — The client's account id.</li>
     *          <li>{@code token} — The client's token id and type.</li>
     *        </ul>
     */
    @PostMapping(value = "app/logout")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogout(@RequestBody LogoutModel body, HttpServletRequest request) {
        if(Application.getPropertiesInfo().enable_heartbeat) {
            Guard.getIpStartTimes().remove(request.getRemoteAddr());
            Guard.getIpStartTimes2().remove(request.getRemoteAddr());
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", null));
    }


    // Classes
    public static class LogoutModel {
        public String aid;
        public JsonNode token;
    }
}