package org.emilieps.bootspring.routes.combo;

// Imports
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.emilieps.Application;
import org.emilieps.bootspring.data.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/combo/guard", "hk4e_cn/combo/guard", "combo/guard"}, produces = "application/json")
public final class Guard implements Response {
    @Getter private static final Map<String, Instant> ipStartTimes = new ConcurrentHashMap<>();
    @Getter private static final Map<String, Instant> ipStartTimes2 = new ConcurrentHashMap<>();

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/guard/api/ping">https://devapi-takumi.mihoyo.com/combo/guard/api/ping</a><br><br>
     *  Description: Checks if the player is playing too long (Anti addiction api, available only in chinese version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br>
     */
    @PostMapping(value = "api/ping")
    public ResponseEntity<LinkedHashMap<String, Object>> sendPing(HttpServletRequest request) {
        if (Application.getPropertiesInfo().enable_heartbeat) {
            Instant now = Instant.now();
            ipStartTimes.putIfAbsent(request.getRemoteAddr(), now);

            long elapsedSeconds = now.getEpochSecond() - ipStartTimes.get(request.getRemoteAddr()).getEpochSecond();
            long limitSeconds = Application.getPropertiesInfo().heartbeat_internal * 60L;
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            if (elapsedSeconds >= limitSeconds) {
                data.put("stop", true);
                data.put("msg", "已达到防沉迷限制");
                data.put("interval", 1);
            } else {
                long remainingMinutes = (limitSeconds - elapsedSeconds) / 60;
                remainingMinutes = Math.max(1, remainingMinutes);

                data.put("stop", false);
                data.put("msg", "");
                data.put("interval", remainingMinutes);
            }

            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", data));
        }
        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", null));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/guard/api/ping2">https://devapi-takumi.mihoyo.com/combo/guard/api/ping2</a><br><br>
     *  Description: Checks if the player is playing too long (Anti addiction api, available only in chinese version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br>
     */
    @RequestMapping(value = "api/ping2")
    public ResponseEntity<LinkedHashMap<String, Object>> SendPing2(HttpServletRequest request) {
        if (Application.getPropertiesInfo().enable_heartbeat) {
            Instant now = Instant.now();
            ipStartTimes2.putIfAbsent(request.getRemoteAddr(), now);
            long elapsedSeconds = now.getEpochSecond() - ipStartTimes2.get(request.getRemoteAddr()).getEpochSecond();
            long limitSeconds = 300 * 60L;

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            if (elapsedSeconds >= limitSeconds) {
                data.put("interval", 1);
                data.put("banned", true);
                data.put("msg", "已达到防沉迷限制");
            } else {
                long remainingMinutes = (limitSeconds - elapsedSeconds) / 60;
                remainingMinutes = Math.max(1, remainingMinutes);

                data.put("interval", remainingMinutes);
                data.put("banned", false);
                data.put("msg", null);
            }

            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", data));
        }
        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", null));
    }
}