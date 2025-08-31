package org.emilieps.bootspring.routes.common;

// Imports
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_FAIL;
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.emilieps.Application;
import org.emilieps.bootspring.data.Response;
import org.emilieps.database.DBUtils;
import org.emilieps.libraries.EncryptionManager;
import org.emilieps.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "common/h5log", produces = "application/json")
public final class H5Log implements Response {
    private final String[] allowedTopics = {"plat_apm_sdk", "plat_explog_sdk_v2", "plat_account_sdk", "plat_crash_sdk"};

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/common/h5log/log/batch">https://devapi-takumi.mihoyo.com/common/h5log/log/batch</a><br><br>
     *  Description: Uploads the H5 network log.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code topic} — The topic id.</li>
     *          <li>{@code data} — The encrypted data using RC4 stream cipher.</li>
     *        </ul>
     */
    @PostMapping(value = "log/batch")
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogBatch(@RequestParam String topic, @RequestBody String data, HttpServletRequest request) {
        if(!Arrays.asList(allowedTopics).contains(topic)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknownh5topic", request.getRemoteAddr(), topic));
            return ResponseEntity.ok(this.makeResponse(RETCODE_FAIL, "topic not allowed", null));
        }

        try {
            data = new String(EncryptionManager.decodeH5Log(Base64.getDecoder().decode(Objects.requireNonNull(JsonLoader.parseJsonSafe(data)).path("data").asText().replaceAll("\\r\\n|\\r|\\n",""))));
            JsonNode root = JsonLoader.parseJsonSafe(data);
            if (root == null || !root.has("data") || !root.get("data").isArray()) {
                return ResponseEntity.ok(this.makeResponse(RETCODE_FAIL, "请求格式错误", null));
            }

            for (JsonNode event : root.get("data")) {
                if (!event.hasNonNull("@timestamp") ||
                        !event.hasNonNull("client_type") ||
                        !event.hasNonNull("device_id") ||
                        !event.hasNonNull("device_name") ||
                        !event.hasNonNull("sys_version")) {
                    return ResponseEntity.ok(this.makeResponse(RETCODE_FAIL, "请求格式错误", null));
                }
            }

            DBUtils.saveTelemetryLog(data, "h5logs");
            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "success", null));
        } catch (Exception e) {
            Application.getLogger().error("json unmarshal failed", e);
            return ResponseEntity.ok(this.makeResponse(RETCODE_FAIL, "json unmarshal failed", null));
        }
    }
}