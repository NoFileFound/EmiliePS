package org.genshinimpact.webserver.routes.telemetry;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_FAIL;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import org.genshinimpact.database.Database;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.models.telemetry.ApmDataUploadModel;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.responses.TsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class APMController {
    /**
     * Source: <a href="https://apm-log-upload.mihoyo.com/_ts">https://apm-log-upload.mihoyo.com/_ts</a><br><br>
     * Description: Fetches the current timestamp.<br><br>
     * Method: GET<br>
     * Content-Type: application/json<br>
     */
    @GetMapping(value = "_ts")
    public ResponseEntity<TsResponse> SendTS() {
        return ResponseEntity.ok(new TsResponse(System.currentTimeMillis()));
    }

    /**
     * Source: <a href="https://apm-log-upload.mihoyo.com/ping">https://apm-log-upload.mihoyo.com/ping</a><br><br>
     * Description: -<br><br>
     * Method: GET<br>
     * Content-Type: String<br>
     */
    @GetMapping(value = "ping")
    public ResponseEntity<String> SendPing() {
        return ResponseEntity.ok("ok");
    }

    /**
     * Source: <a href="https://apm-log-upload.mihoyo.com/apm/dataUpload">https://apm-log-upload.mihoyo.com/apm/dataUpload</a><br><br>
     * Description: Collects information about the application performance management.<br><br>
     * Method: POST<br>
     * Content-Type: application/json<br><br>
     * Parameters:<br>
     * <ul>
     *   <li>{@code applicationId} — The identifier of the application ({@code 900001}).</li>
     *   <li>{@code applicationName} — The name of the application ({@code mihoyosdk}).</li>
     *   <li>{@code eventId} — The identifier of the reported event.</li>
     *   <li>{@code eventName} — The name of the reported event.</li>
     *   <li>{@code eventTime} — The timestamp of the event occurrence.</li>
     *   <li>{@code msgId} — The message identifier for this report.</li>
     *   <li>{@code uploadContent} — The uploaded content containing detailed information about the application performance.</li>
     * </ul>
     */
    @PostMapping("apm/dataUpload")
    public ResponseEntity<Response<?>> ReceiveAPMLog(HttpServletRequest request, @RequestHeader(value = "CONTENT-MD5", required = false) String contentMd5, @RequestHeader(value = "Content-Type", required = false) String contentType, @RequestHeader(value = "DATE", required = false) String date, @RequestHeader(value = "cms-signature", defaultValue = "hmac-sha1") String cmsSignature, @RequestHeader(value = "Authorization", required = false) String authorization) {
        ApmDataUploadModel body;
        try {
            byte[] payload = request.getInputStream().readAllBytes();
            String message = String.join("\n", request.getMethod(), contentMd5 != null ? contentMd5 : "", contentType != null ? contentType : "", date != null ? date : "", cmsSignature);
            String computedHmac = CryptoUtils.getHMAC1(message, "mihoyo2020hk4e");
            if(!computedHmac.equals(authorization)) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }

            String payloadMd5 = CryptoUtils.getMd5(payload);
            if(!payloadMd5.equalsIgnoreCase(contentMd5)) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }

            body = JsonUtils.readList(new ByteArrayInputStream(payload), ApmDataUploadModel.class).get(0);
            if(body == null
                    || body.applicationId == null
                    || body.applicationName == null
                    || body.msgId == null
                    || body.eventTime == null
                    || body.eventId == null
                    || body.eventName == null
                    || body.uploadContent == null
                    || body.uploadContent.ReportId == null
                    || body.uploadContent.LocalTimestamp == null
                    || body.uploadContent.OccurTimestamp == null

                    // APM Info
                    || body.uploadContent.APMInfo == null
                    || body.uploadContent.APMInfo.SDKVersion == null

                    // Application info
                    || body.uploadContent.AppInfo == null
                    || body.uploadContent.AppInfo.AppId == null
                    || body.uploadContent.AppInfo.Area == null
                    || body.uploadContent.AppInfo.AppVersion == null
                    || body.uploadContent.AppInfo.PackageName == null
                    || body.uploadContent.AppInfo.Region == null
                    || body.uploadContent.AppInfo.Channel == null
                    || body.uploadContent.AppInfo.UserDeviceId == null
                    || body.uploadContent.AppInfo.UserId == null
                    || body.uploadContent.AppInfo.CompileType == null || body.uploadContent.AppInfo.CompileType.isBlank()
                    || body.uploadContent.AppInfo.LifecycleId == null

                    // Device Information
                    || body.uploadContent.DeviceInfo == null
                    || body.uploadContent.DeviceInfo.Brand == null
                    || body.uploadContent.DeviceInfo.CPUArchitecture == null
                    || body.uploadContent.DeviceInfo.DeviceId == null || body.uploadContent.DeviceInfo.DeviceId.isBlank()
                    || body.uploadContent.DeviceInfo.DeviceModel == null || body.uploadContent.DeviceInfo.DeviceModel.isBlank()
                    || body.uploadContent.DeviceInfo.DeviceName == null
                    || body.uploadContent.DeviceInfo.IsRoot == null
                    || body.uploadContent.DeviceInfo.Platform == null || body.uploadContent.DeviceInfo.Platform.isBlank()
                    || body.uploadContent.DeviceInfo.Rom == null
                    || body.uploadContent.DeviceInfo.SystemVersion == null) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        Database.saveLog(body, "apm_logs");
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
    }
}