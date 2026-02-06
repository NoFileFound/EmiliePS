package org.genshinimpact.webserver.routes.telemetry;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_FAIL;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.Application;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.models.telemetry.H5LogModel;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = "application/json")
public final class LogController {
    /**
     * Source: <a href="http://overseauspider.yuanshen.com:8888/log">http://overseauspider.yuanshen.com:8888/log</a><br><br>
     * Description: Receives client runtime log data from the game client.<br><br>
     * Method: POST<br>
     * Content-Type: application/json<br>
     */
    @PostMapping(value = "log")
    public ResponseEntity<Response<?>> ReceiveH5Log(HttpServletRequest request) {
        H5LogModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), H5LogModel.class);
            if(body == null ||
                    body.userName == null ||
                    body.time == null ||
                    body.frame == null ||
                    body.stackTrace == null ||
                    body.logStr == null ||
                    body.logType == null ||
                    body.deviceName == null ||
                    body.deviceModel == null ||
                    body.operatingSystem == null ||
                    body.version == null ||
                    body.exceptionSerialNum == null ||
                    body.pos == null ||
                    body.guid == null ||
                    body.errorCode == null ||
                    body.errorCodeToPlatform == null ||
                    body.serverName == null ||
                    body.subErrorCode == null ||
                    body.uid == null ||
                    body.cpuInfo == null ||
                    body.gpuInfo == null ||
                    body.memoryInfo == null ||
                    body.clientIp == null ||
                    body.errorLevel == null ||
                    body.errorCategory == null ||
                    body.notifyUserName == null ||
                    body.auid == null ||
                    body.buildUrl == null) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        StringBuilder sb = new StringBuilder("[H5Log] " + body.logStr.replaceAll("[\\s\\n\\r]+$", ""));
        if(!body.stackTrace.isBlank()) {
            sb.append(System.lineSeparator()).append(body.stackTrace.replaceAll("[\\s\\n\\r]+$", ""));
        }

        switch (body.logType) {
            case "Warning":
                Application.getLogger().warning(sb.toString());
                break;
            case "Error":
            case "Exception":
                Application.getLogger().severe(sb.toString());
                break;
            default:
                Application.getLogger().info(sb.toString());
                break;
        }

        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/common/h5log/log/batch">https://devapi-takumi.mihoyo.com/common/h5log/log/batch</a><br><br>
     * Description: Receives client runtime log data from the game client.<br><br>
     * Method: POST<br>
     * Content-Type: application/json<br><br>
     * Parameters:<br>
     * <ul>
     *   <li>{@code topic} — The topic name (log identifier).</li>
     *   <li>{@code data} — The encrypted data using RC4 stream cipher.</li>
     * </ul>
     */
    @PostMapping(value = "common/h5log/log/batch")
    public ResponseEntity<Response<?>> ReceiveH5LogBatch(@RequestParam String topic, @RequestBody String data) {
        if(!topic.equals("plat_apm_sdk") // APM
                && !topic.equals("plat_explog_sdk_v2") // SDK
                && !topic.equals("plat_account_sdk") // Login sdk
                && !topic.equals("plat_crash_sdk")) { // Crash
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "topic not allowed"));
        }

        var jsonObj = JsonUtils.read(data);
        if(jsonObj == null) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "json unmarshal failed"));
        }

        JsonNode body = CryptoUtils.decodeH5Log(jsonObj.path("data").asText());
        if(body == null) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "json unmarshal failed"));
        }

        if(!body.has("data") || !body.get("data").isArray()) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        /// TODO: [Unfinished #2] Investigate what mihoyo is doing with this.
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "success"));
    }
}