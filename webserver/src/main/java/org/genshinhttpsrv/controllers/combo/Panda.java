package org.genshinhttpsrv.controllers.combo;

// Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.api.enums.ApplicationId;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.genshinhttpsrv.libraries.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/combo/panda/qrcode", "hk4e_cn/combo/panda/qrcode", "combo/panda/qrcode"}, produces = "application/json")
public final class Panda implements Response {
    private final LinkedHashMap<String, QRCodeInfo> qrCodeInfoMap = new LinkedHashMap<>();

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/confirm">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/confirm</a><br><br>
     *  Description: Confirms the qr code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qrcode ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *          <li>{@code payload} — The given payload.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "confirm")
    public ResponseEntity<LinkedHashMap<String, Object>> SendQRCodeConfirm(@RequestBody ConfirmModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(!Application.getPropertiesInfo().enable_qrcode_login) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_disabled"), null));
        }

        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = this.qrCodeInfoMap.get(body.device);
        if(myTicket == null || !myTicket.id.equals(body.ticket)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_expired"), null));
        }

        if(!body.payload.has("proto") && !body.payload.has("raw")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        var myAccount = DBUtils.findAccountById(body.payload.get("raw").get("uid").asLong());
        if(myAccount == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslationManager().get(lang, "retcode_network_at_risk"), null));
        }

        if(!myAccount.getApprovedDevices().contains(body.device)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_LOGIN_NEW_LOCATION_FOUND, Application.getTranslationManager().get(lang, "retcode_login_new_location_found"), null));
        }

        if(myTicket.getState() != QRCodeInfo.QRCodeState.Scanned) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        myTicket.setState(QRCodeInfo.QRCodeState.Confirmed);
        myTicket.setPayload(body.payload.get("raw").get("token").asText());
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>()));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/fetch">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/fetch</a><br><br>
     *  Description: Fetches the qr code link.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "fetch")
    public ResponseEntity<LinkedHashMap<String, Object>> SendQRCodeFetch(@RequestBody FetchModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(!Application.getPropertiesInfo().enable_qrcode_login) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_disabled"), null));
        }

        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        this.qrCodeInfoMap.put(body.device, new QRCodeInfo());
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("url", String.format("https://user.mihoyo.com/qr_code_in_game.html?app_id=%d&app_name=原神&bbs=true&biz_key=hk4e_cn&expire=%d&ticket=%s", body.app_id.getValue(), qrCodeInfoMap.get(body.device).time, qrCodeInfoMap.get(body.device).id));
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/query">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/query</a><br><br>
     *  Description: Scans the qr code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qrcode ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "query")
    public ResponseEntity<LinkedHashMap<String, Object>> SendQRCodeQuery(@RequestBody QueryModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) throws JsonProcessingException {
        if(!Application.getPropertiesInfo().enable_qrcode_login) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_disabled"), null));
        }

        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = this.qrCodeInfoMap.get(body.device);
        if(myTicket == null || !myTicket.id.equals(body.ticket)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_expired"), null));
        }

        if(myTicket.getState() != QRCodeInfo.QRCodeState.Confirmed) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("stat", myTicket.getState());
                put("payload", new LinkedHashMap<>(Map.of(
                    "proto", "Raw",
                    "raw", "",
                    "ext", ""
                )));
            }}));
        } else {
            var myAccount = DBUtils.findAccountByToken(myTicket.getPayload());
            if(myAccount == null) {
                return ResponseEntity.ok(this.makeResponse(Retcode.RET_SYSTEM_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
            }

            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<String, Object>() {{
                put("stat", myTicket.getState());
                put("payload", new LinkedHashMap<String, Object>() {{
                    put("proto", "Account");
                    put("raw", JsonLoader.toJson(new LinkedHashMap<String, Object>() {{
                        put("uid", myAccount.get_id());
                        put("name", myAccount.getName());
                        put("email", myAccount.getEmailAddress());
                        put("mobile", myAccount.getMobileNumber());
                        put("mobile_area", myAccount.getMobileNumberArea());
                        put("is_email_verify", myAccount.getIsEmailVerified());
                        put("realname", StringUtils.filterString(myAccount.getRealname()));
                        put("identity_card", StringUtils.filterString(myAccount.getIdentityCard()));
                        put("token", myAccount.getGameToken());
                        put("country", myAccount.getCountryCode());
                    }}));
                    put("ext", "");
                }});
            }}));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/scan">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/scan</a><br><br>
     *  Description: Scans the qr code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qrcode ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "scan")
    public ResponseEntity<LinkedHashMap<String, Object>> SendQRCodeScan(@RequestBody ScanModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(!Application.getPropertiesInfo().enable_qrcode_login) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_disabled"), null));
        }

        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        var myTicket = this.qrCodeInfoMap.get(body.device);
        if(myTicket == null || !myTicket.id.equals(body.ticket)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_QR_CODE_EXPIRED, Application.getTranslationManager().get(lang, "retcode_qr_code_expired"), null));
        }

        myTicket.setState(QRCodeInfo.QRCodeState.Scanned);
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>()));
    }


    // Classes
    public static class ConfirmModel {
        public ApplicationId app_id;
        public String ticket;
        public JsonNode payload;
        public String device;
    }

    public static class FetchModel {
        public ApplicationId app_id;
        public String device;
    }

    public static class QueryModel {
        public ApplicationId app_id;
        public String ticket;
        public String device;
    }

    public static class ScanModel {
        public ApplicationId app_id;
        public String ticket;
        public String device;
    }

    @Getter
    public static class QRCodeInfo {
        private final String id;
        private final int time;
        private final Long fetchedTime;
        @Setter private QRCodeState state;
        @Setter private String payload;

        public QRCodeInfo() {
            this.id = EncryptionManager.generateRandomKey(10);
            this.time = 1440;
            this.fetchedTime = System.currentTimeMillis();
            this.state = QRCodeState.Init;
        }

        public enum QRCodeState {
            Init,
            Scanned,
            Confirmed
        }
    }
}