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
@RequestMapping(produces = "application/json")
public final class Sdk {
    /**
     *  Source: <a href="https://sdk-log-upload.mihoyo.com/sdk/dataUpload">https://sdk-log-upload.mihoyo.com/sdk/dataUpload</a><br><br>
     *  Description: Collects information about the PC/PS4 sdk.<br><br>
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
     *          <li>{@code uploadContent} — The uploaded content such as deviceInfo, logInfo, userInfo and versionInfo.</li>
     *        </ul>
     */
    @PostMapping(value = {"sdk/dataUpload", "client/event/dataUpload"})
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
                        !event.hasNonNull("msgId") ||
                        !event.hasNonNull("uploadContent")) {
                    return ResponseEntity.ok(new LinkedHashMap<>() {{
                        put("code", RETCODE_FAIL);
                        put("message", "请求格式错误");
                    }});
                }

                JsonNode uploadContent = event.get("uploadContent");
                if(event.has("eventName") && event.get("eventName").toString().replace("\"", "").equals("ClientReport")) {
                    if (!uploadContent.has("device_info") || uploadContent.get("device_info").isNull()
                            || !uploadContent.has("log_info") || uploadContent.get("log_info").isNull()
                            || !uploadContent.has("user_info") || uploadContent.get("user_info").isNull()
                            || !uploadContent.has("launchTraceId") || uploadContent.get("launchTraceId").isNull()
                            || !uploadContent.has("version_info") || uploadContent.get("version_info").isNull()) {
                        return ResponseEntity.ok(new LinkedHashMap<>() {{
                            put("code", RETCODE_FAIL);
                            put("message", "请求格式错误");
                        }});
                    }
                } else {
                    if (!uploadContent.has("deviceInfo") || uploadContent.get("deviceInfo").isNull()
                            || !uploadContent.has("logInfo") || uploadContent.get("logInfo").isNull()
                            || !uploadContent.has("userInfo") || uploadContent.get("userInfo").isNull()
                            || !uploadContent.has("launchTraceId") || uploadContent.get("launchTraceId").isNull()
                            || !uploadContent.has("versionInfo") || uploadContent.get("versionInfo").isNull()) {
                        return ResponseEntity.ok(new LinkedHashMap<>() {{
                            put("code", RETCODE_FAIL);
                            put("message", "请求格式错误");
                        }});
                    }
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