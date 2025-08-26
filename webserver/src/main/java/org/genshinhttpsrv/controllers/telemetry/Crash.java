package org.genshinhttpsrv.controllers.telemetry;

// Imports
import static org.genshinhttpsrv.api.Retcode.RETCODE_FAIL;
import static org.genshinhttpsrv.api.Retcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "crash/", produces = "application/json")
public final class Crash {
    /**
     *  Source: <a href="https://log-upload.mihoyo.com/crash/dataUpload">https://log-upload.mihoyo.com/crash/dataUpload</a><br><br>
     *  Description: Collects information about the incident/crash.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code applicationId} — The application id.</li>
     *          <li>{@code applicationName} — The application name (hk4e).</li>
     *          <li>{@code eventId} — The event id.</li>
     *          <li>{@code eventName} — The event name.</li>
     *          <li>{@code eventTime} — The event timestamp.</li>
     *          <li>{@code msgId} — The message id.</li>
     *          <li>{@code uploadContent} — The uploaded content, contains information about the incident information and user/platform information.</li>
     *        </ul>
     */
    @PostMapping("dataUpload")
    public ResponseEntity<LinkedHashMap<String, Object>> SendDataUpload(@RequestBody String data) {
        try {
            JsonNode root = JsonLoader.parseJsonSafe(data);
            if (root == null || !root.isArray() || root.isEmpty()) {
                return ResponseEntity.ok(new LinkedHashMap<>() {{
                    put("code", RETCODE_FAIL);
                    put("message", "请求格式错误");
                }});
            }

            for (JsonNode event : root) {
                if (!event.hasNonNull("applicationId") ||
                        !event.hasNonNull("applicationName") ||
                        !event.hasNonNull("eventId") ||
                        !event.hasNonNull("eventName") ||
                        !event.hasNonNull("eventTime") ||
                        !event.hasNonNull("msgID") ||
                        !event.hasNonNull("uploadContent")) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }

                JsonNode uploadContent = event.get("uploadContent");
                if (!uploadContent.has("clientIp") ||
                        !uploadContent.has("errorCode") ||
                        !uploadContent.has("errorCategory") ||
                        !uploadContent.has("errorCode") ||
                        !uploadContent.has("errorLevel") ||
                        !uploadContent.has("isRelease") ||
                        !uploadContent.has("logType") ||
                        !uploadContent.has("serverName") ||
                        !uploadContent.has("stackTrace") ||
                        !uploadContent.has("subErrorCode") ||
                        !uploadContent.has("time") ||
                        !uploadContent.has("message") ||
                        !uploadContent.has("notifyUser") ||
                        !uploadContent.has("guid") ||
                        !uploadContent.has("serverName")) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }
            }

            DBUtils.saveTelemetryLog(data, "crashes");
            return ResponseEntity.ok(new LinkedHashMap<>() {{
                put("code", RETCODE_SUCC);
                put("message", "OK");
            }});
        } catch (Exception ignored) {
            return ResponseEntity.ok(new LinkedHashMap<>() {{
                put("code", RETCODE_FAIL);
                put("message", "请求格式错误");
            }});
        }
    }
}