package org.genshinimpact.webserver.routes.telemetry;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_FAIL;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.models.telemetry.CrashDataUploadModel;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.stores.CrashLogStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "crash/", produces = "application/json")
public final class CrashController {
    private final CrashLogStore crashLogStore;

    /**
     * Creates a new {@code CrashDataUploadController}.
     * @param crashLogStore The crash log storage component used to persist incoming crash reports.
     */
    public CrashController(CrashLogStore crashLogStore) {
        this.crashLogStore = crashLogStore;
    }

    /**
     * Source: <a href="https://devlog-upload.mihoyo.com/crash/dataUpload">https://devlog-upload.mihoyo.com/crash/dataUpload</a><br><br>
     * Description: Collects information about an incident.<br><br>
     * Method: POST<br>
     * Content-Type: application/json<br><br>
     * Parameters:<br>
     * <ul>
     *   <li>{@code applicationId} — The identifier of the application ({@code 100001}).</li>
     *   <li>{@code applicationName} — The name of the application ({@code hk4e}).</li>
     *   <li>{@code eventId} — The identifier of the reported event.</li>
     *   <li>{@code eventName} — The name of the reported event.</li>
     *   <li>{@code eventTime} — The timestamp of the event occurrence.</li>
     *   <li>{@code msgId} — The message identifier for this report.</li>
     *   <li>{@code uploadContent} — The uploaded content containing detailed information about the incident.</li>
     * </ul>
     */
    @PostMapping("dataUpload")
    public ResponseEntity<Response<?>> ReceiveCrashLog(HttpServletRequest request, @RequestHeader(value = "CONTENT-MD5", required = false) String contentMd5, @RequestHeader(value = "Content-Type", required = false) String contentType, @RequestHeader(value = "cms-signature", defaultValue = "hmac-sha1") String cmsSignature, @RequestHeader(value = "Authorization", required = false) String authorization) {
        CrashDataUploadModel body;
        try {
            byte[] payload = request.getInputStream().readAllBytes();
            String message = String.join("\n", request.getMethod(), contentMd5 != null ? contentMd5 : "", contentType != null ? contentType : "", "", cmsSignature);
            String computedHmac = CryptoUtils.getHMAC1(message, "mihoyo2020hk4e");
            if(!computedHmac.equals(authorization)) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "上报数据校验失败，请检查"));
            }

            body = JsonUtils.readList(new ByteArrayInputStream(payload), CrashDataUploadModel.class).get(0);
            String payloadMd5 = CryptoUtils.getMd5(JsonUtils.toJsonString(body).getBytes());
            if(!payloadMd5.equalsIgnoreCase(contentMd5)) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "上报数据校验失败，请检查"));
            }

            if(body == null
                    || body.applicationId == null
                    || body.applicationName == null
                    || body.msgID == null
                    || body.eventTime == null || body.eventTime.isBlank()
                    || body.eventId == null
                    || body.eventName == null
                    || body.uploadContent == null

                    // The additional information of the incident.
                    || body.uploadContent.error_code == null
                    || body.uploadContent.message == null || body.uploadContent.message.length() < 10
                    || body.uploadContent.user_id == null
                    || body.uploadContent.time == null
                    || body.uploadContent.stackTrace == null
                    || body.uploadContent.exceptionSerialNum == null
                    || body.uploadContent.frame == null
                    || body.uploadContent.deviceModel == null
                    || body.uploadContent.deviceName == null
                    || body.uploadContent.operatingSystem == null
                    || body.uploadContent.userName == null
                    || body.uploadContent.version == null
                    || body.uploadContent.guid == null
                    || body.uploadContent.isRelease == null
                    || body.uploadContent.logType == null
                    || body.uploadContent.subErrorCode == null
                    || body.uploadContent.cpuInfo == null
                    || body.uploadContent.gpuInfo == null
                    || body.uploadContent.memoryInfo == null
                    || body.uploadContent.clientIp == null
                    || body.uploadContent.errorLevel == null
                    || body.uploadContent.errorCategory == null
                    || body.uploadContent.notifyUser == null) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        this.crashLogStore.insert(body);
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
    }
}