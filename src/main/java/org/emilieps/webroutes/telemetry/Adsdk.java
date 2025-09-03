package org.emilieps.webroutes.telemetry;

// Imports
import static org.emilieps.data.HttpRetcode.RETCODE_FAIL;
import static org.emilieps.data.HttpRetcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.JsonLib;
import org.emilieps.library.MongodbLib;

@RestController
@RequestMapping(value = "adsdk/", produces = "application/json")
public final class Adsdk {
    /**
     *  Source: <a href="https://ad-log-upload.mihoyo.com/adsdk/dataUpload">https://ad-log-upload.mihoyo.com/adsdk/dataUpload</a><br><br>
     *  Description: Collects information about the android sdk.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code applicationId} — The application id.</li>
     *          <li>{@code applicationName} — The application name (adsdk).</li>
     *          <li>{@code eventId} — The event id.</li>
     *          <li>{@code eventName} — The event name.</li>
     *          <li>{@code eventTime} — The event timestamp.</li>
     *          <li>{@code msgId} — The message id.</li>
     *          <li>{@code uploadContent} — The uploaded content such as attributionInfo.</li>
     *        </ul>
     */
    @PostMapping("dataUpload")
    public ResponseEntity<LinkedHashMap<String, Object>> SendDataUpload(@RequestBody String data) {
        try {
            JsonNode root = JsonLib.parseJsonSafe(data);
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
                if (!uploadContent.has("attributionInfo") || uploadContent.get("attributionInfo").isNull()) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }
            }

            if(Application.getApplicationConfig().is_debug) {
                MongodbLib.saveTelemetryLog(data, "adsdk");
            }

            return ResponseEntity.ok(new LinkedHashMap<>() {{
                put("code", RETCODE_SUCC);
            }});
        } catch (Exception ignored) {
            return ResponseEntity.ok(new LinkedHashMap<>() {{
                put("code", RETCODE_FAIL);
                put("message", "请求格式错误");
            }});
        }
    }
}