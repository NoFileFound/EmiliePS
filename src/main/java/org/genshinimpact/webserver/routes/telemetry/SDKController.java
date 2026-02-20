package org.genshinimpact.webserver.routes.telemetry;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_FAIL;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.models.telemetry.AndroidSdkDataUploadModel;
import org.genshinimpact.webserver.models.telemetry.SdkDataUploadModel;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = "application/json")
public final class SDKController {
    /**
     * Source: <a href="https://devlog-upload.mihoyo.com/sdk/dataUpload">https://devlog-upload.mihoyo.com/sdk/dataUpload</a><br><br>
     * Description: Collects information about the client's behavior on the game.<br><br>
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
     *   <li>{@code uploadContent} — The uploaded content containing detailed information about the client's behavior.</li>
     * </ul>
     */
    @PostMapping(value = {"sdk/dataUpload", "client/event/dataUpload"})
    public ResponseEntity<Response<?>> ReceiveSDKLog(HttpServletRequest request, @RequestHeader(value = "CONTENT-MD5", required = false) String contentMd5, @RequestHeader(value = "Content-Type", required = false) String contentType, @RequestHeader(value = "DATE", required = false) String date, @RequestHeader(value = "cms-signature", defaultValue = "hmac-sha1") String cmsSignature, @RequestHeader(value = "Authorization", required = false) String authorization) {
        SdkDataUploadModel body;
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

            body = JsonUtils.readList(new ByteArrayInputStream(payload), SdkDataUploadModel.class).get(0);
            if(body == null
                    || body.applicationId == null
                    || body.applicationName == null || !body.applicationName.equals("mihoyosdk")
                    || body.msgId == null
                    || body.eventTime == null
                    || body.eventId == null
                    || body.eventName == null
                    || body.uploadContent == null

                    // Device information
                    || body.uploadContent.deviceInfo == null
                    || body.uploadContent.deviceInfo.platform == null
                    || body.uploadContent.deviceInfo.deviceId == null
                    || body.uploadContent.deviceInfo.bundleId == null
                    || body.uploadContent.deviceInfo.addressMac == null
                    || body.uploadContent.deviceInfo.processorCount == null
                    || body.uploadContent.deviceInfo.processorFrequency == null

                    // User information
                    || body.uploadContent.userInfo == null
                    || body.uploadContent.userInfo.channelId == null

                    // Version information
                    || body.uploadContent.versionInfo == null
                    || body.uploadContent.versionInfo.clientVersion == null
                    || body.uploadContent.versionInfo.logVersion == null

                    // Log information
                    || body.uploadContent.logInfo == null
                    || body.uploadContent.logInfo.logTime == null
                    || body.uploadContent.logInfo.actionId == null
                    || body.uploadContent.logInfo.actionName == null) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }

            DBUtils.saveLogCache(body, "sdk_logs");
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }
    }

    /**
     * Source: <a href="https://devlog-upload.mihoyo.com/adsdk/dataUpload">https://devlog-upload.mihoyo.com/adsdk/dataUpload</a><br><br>
     * Description: Collects information about the client's behavior on the game. (Android platform)<br><br>
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
     *   <li>{@code uploadContent} — The uploaded content containing detailed information about the client's behavior.</li>
     * </ul>
     */
    @PostMapping(value = "adsdk/dataUpload")
    public ResponseEntity<Response<?>> ReceiveAndroidSDKLog(HttpServletRequest request, @RequestHeader(value = "CONTENT-MD5", required = false) String contentMd5, @RequestHeader(value = "Content-Type", required = false) String contentType, @RequestHeader(value = "DATE", required = false) String date, @RequestHeader(value = "cms-signature", defaultValue = "hmac-sha1") String cmsSignature, @RequestHeader(value = "Authorization", required = false) String authorization) {
        AndroidSdkDataUploadModel body;
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

            body = JsonUtils.readList(new ByteArrayInputStream(payload), AndroidSdkDataUploadModel.class).get(0);
            if(body == null
                    || body.applicationId == null
                    || body.applicationName == null || !body.applicationName.equals("adsdk")
                    || body.msgId == null
                    || body.eventTime == null
                    || body.eventId == null
                    || body.eventName == null
                    || body.uploadContent == null
                    || body.uploadContent.attributionInfo == null

                    // Attribution info
                    || body.uploadContent.attributionInfo.launch_trace_id == null || body.uploadContent.attributionInfo.launch_trace_id.isBlank()
                    || body.uploadContent.attributionInfo.androidid == null || body.uploadContent.attributionInfo.androidid.isBlank()
                    || body.uploadContent.attributionInfo.pkgname == null || body.uploadContent.attributionInfo.pkgname.isBlank()
                    || body.uploadContent.attributionInfo.event_name == null || body.uploadContent.attributionInfo.event_name.isBlank()) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        DBUtils.saveLogCache(body, "android_sdk_logs");
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
    }
}