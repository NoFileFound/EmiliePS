package org.genshinhttpsrv.controllers.telemetry;

// Imports
import static org.genshinhttpsrv.api.Retcode.RETCODE_FAIL;
import static org.genshinhttpsrv.api.Retcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"loginsdk/"}, produces = "application/json")
public final class Loginsdk {
    /**
     *  Source: <a href="https://log-upload.mihoyo.com/loginsdk/dataUpload">https://log-upload.mihoyo.com/loginsdk/dataUpload</a><br><br>
     *  Description: Collects information about the login sdk.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code applicationId} — The application id.</li>
     *          <li>{@code applicationName} — The application name (loginsdk).</li>
     *          <li>{@code eventId} — The event id.</li>
     *          <li>{@code eventName} — The event name.</li>
     *          <li>{@code eventTime} — The event timestamp.</li>
     *          <li>{@code msgId} — The message id.</li>
     *          <li>{@code uploadContent} — The uploaded content such as deviceInfo, logInfo, userInfo and versionInfo.</li>
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
                        !event.hasNonNull("msgId") ||
                        !event.hasNonNull("uploadContent")) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }

                JsonNode uploadContent = event.get("uploadContent");
                if (!uploadContent.has("deviceinfo") || uploadContent.get("deviceinfo").isNull()
                        || !uploadContent.has("loginfo") || uploadContent.get("loginfo").isNull()
                        || !uploadContent.has("userinfo") || uploadContent.get("userinfo").isNull()
                        || !uploadContent.has("eventTimeMs") || uploadContent.get("eventTimeMs").isNull()
                        || !uploadContent.has("versioninfo") || uploadContent.get("versioninfo").isNull()) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }
            }

            if(Application.getPropertiesInfo().is_debug) {
                DBUtils.saveTelemetryLog(data, "sdk");
            }

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