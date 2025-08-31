package org.emilieps.bootspring.routes.telemetry;

// Imports
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_FAIL;
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.database.DBUtils;
import org.emilieps.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"ys_custom/"}, produces = "application/json")
public final class Yscustom {
    /**
     *  Source: <a href="http://ys-log-upload.mihoyo.com/ys_custom/dataUpload">http://ys-log-upload.mihoyo.com/ys_custom/dataUpload</a><br><br>
     *  Description: Collects information about the PC/PS4 sdk.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code applicationId} — The application id.</li>
     *          <li>{@code applicationName} — The application name (adsdk).</li>
     *          <li>{@code eventTime} — The event timestamp.</li>
     *          <li>{@code msgId} — The message id.</li>
     *          <li>{@code uploadContent} — The uploaded content including the device info and game version.</li>
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
                        !event.hasNonNull("eventTime") ||
                        !event.hasNonNull("msgID") ||
                        !event.hasNonNull("uploadContent")) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }

                JsonNode uploadContent = event.get("uploadContent");
                if (!uploadContent.has("user_id") || uploadContent.get("user_id").isNull()
                        || !uploadContent.has("platform") || uploadContent.get("platform").isNull()
                        || !uploadContent.has("version") || uploadContent.get("version").isNull()
                        || !uploadContent.has("auid") || uploadContent.get("auid").isNull()
                        || !uploadContent.has("serverName") || uploadContent.get("serverName").isNull()) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }
            }

            if(Application.getPropertiesInfo().is_debug) {
                DBUtils.saveTelemetryLog(data, "yscustom");
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