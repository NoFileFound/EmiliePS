package org.genshinimpact.webserver.routes.combo;

//  Imports
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.combo.guard.*;
import org.genshinimpact.webserver.responses.combo.guard.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/combo/guard/api", "hk4e_cn/combo/guard/api", "combo/guard/api"}, produces = "application/json")
public final class ComboGuardController {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/guard/api/ping">https://devapi-takumi.mihoyo.com/combo/guard/api/ping</a><br><br>
     *  Description: Checks if the player is playing too long (Anti addiction api, available only in Chinese version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code combo_token} — The client's combo token.</li>
     *          <li>{@code open_id} — The client's account id.</li>
     *        </ul>
     */
    @PostMapping(value = "ping")
    public ResponseEntity<Response<?>> SendComboPing1(HttpServletRequest request) {
        GuardPing1Model body;
        try {
            body = JsonUtils.read(request.getInputStream(), GuardPing1Model.class);
            if(body == null || body.combo_token == null || body.combo_token.isBlank() || body.open_id == null || body.open_id.isBlank() || body.app_id == AppId.APP_UNKNOWN || body.client_type == ClientType.PLATFORM_UNKNOWN || body.device_id == null || body.device_id.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response()));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.open_id));
            if(myAccount == null || !myAccount.getComboToken().equals(body.combo_token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response()));
            }

            if(!myAccount.getRequireHeartbeat()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response()));
            }

            String ip = request.getRemoteAddr();
            Instant now = Instant.now();
            Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(ip, k -> now);
            long elapsedSeconds = now.getEpochSecond() - start.getEpochSecond();
            if(elapsedSeconds >= 5400) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response(true, "已达到防沉迷限制", 0L)));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response(false, "", (5400 - elapsedSeconds) / 60)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing1Response()));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/guard/api/ping2">https://devapi-takumi.mihoyo.com/combo/guard/api/ping2</a><br><br>
     *  Description: Checks if the player is playing too long (Anti addiction api, available only in Chinese version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code combo_token} — The client's combo token.</li>
     *          <li>{@code open_id} — The client's account id.</li>
     *        </ul>
     */
    @PostMapping(value = "ping2")
    public ResponseEntity<Response<?>> SendComboPing2(HttpServletRequest request) {
        GuardPing1Model body;
        try {
            body = JsonUtils.read(request.getInputStream(), GuardPing1Model.class);
            if(body == null || body.combo_token == null || body.combo_token.isBlank() || body.open_id == null || body.open_id.isBlank() || body.app_id == AppId.APP_UNKNOWN || body.client_type == ClientType.PLATFORM_UNKNOWN || body.device_id == null || body.device_id.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "OK", "参数错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.open_id));
            if(myAccount == null || !myAccount.getComboToken().equals(body.combo_token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "OK", "参数错误"));
            }

            if(!myAccount.getRequireHeartbeat()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "OK", "参数错误"));
            }

            String ip = request.getRemoteAddr();
            Instant now = Instant.now();
            Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(ip, k -> now);
            long elapsedSeconds = now.getEpochSecond() - start.getEpochSecond();
            if(elapsedSeconds >= 5400) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing2Response(true, "已达到防沉迷限制", 0L)));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuardPing2Response(false, "", (5400 - elapsedSeconds) / 60)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "OK", "参数错误"));
        }
    }
}