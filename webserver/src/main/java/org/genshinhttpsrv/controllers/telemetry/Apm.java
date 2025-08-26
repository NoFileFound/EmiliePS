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
@RequestMapping(value = "apm/", produces = "application/json")
public final class Apm {
    /**
     *  Source: <a href="https://apm-log-upload.mihoyo.com/apm/dataUpload">https://apm-log-upload.mihoyo.com/apm/dataUpload</a><br><br>
     *  Description: Collects information about the application performance management.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code applicationId} — The application id.</li>
     *          <li>{@code applicationName} — The application name (apm).</li>
     *          <li>{@code eventId} — The event id.</li>
     *          <li>{@code eventName} — The event name.</li>
     *          <li>{@code eventTime} — The event timestamp.</li>
     *          <li>{@code msgId} — The message id.</li>
     *          <li>{@code uploadContent} — The uploaded content, such as APMInfo, AppInfo and DeviceInfo.</li>
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
                if (!uploadContent.has("APMInfo") || uploadContent.get("APMInfo").isNull()
                        || !uploadContent.has("AppInfo") || uploadContent.get("AppInfo").isNull()
                        || !uploadContent.has("DeviceInfo") || uploadContent.get("DeviceInfo").isNull()
                        || !uploadContent.has("LocalTimestamp") || uploadContent.get("LocalTimestamp").isNull()
                        || !uploadContent.has("OccurTimestamp") || uploadContent.get("OccurTimestamp").isNull()
                        || !uploadContent.has("ReportId") || uploadContent.get("ReportId").isNull()) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }
            }

            if(Application.getPropertiesInfo().is_debug) {
                DBUtils.saveTelemetryLog(data, "apm");
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