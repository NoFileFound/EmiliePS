package org.genshinhttpsrv.controllers.combo;

// Imports
import java.util.LinkedHashMap;
import lombok.Getter;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.api.enums.ApplicationId;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/combo/panda/qrcode", "hk4e_cn/combo/panda/qrcode", "combo/panda/qrcode"}, produces = "application/json")
public final class Panda implements Response {
    private final LinkedHashMap<String, QRCodeInfo> qrCodeInfoMap = new LinkedHashMap<>();

    /**
     *  Source: <a href="https://hk4e-sdk.mihoyo.com/hk4e_cn/combo/panda/qrcode/fetch">https://hk4e-sdk.mihoyo.com/hk4e_cn/combo/panda/qrcode/fetch</a><br><br>
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
    @RequestMapping(value = "fetch")
    public ResponseEntity<LinkedHashMap<String, Object>> SendQRCodeFetch(@RequestBody FetchModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_system_error"), null));
        }

        this.qrCodeInfoMap.put(body.device, new QRCodeInfo());
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("url", String.format("https://user.mihoyo.com/qr_code_in_game.html?app_id=%d&app_name=原神&bbs=true&biz_key=hk4e_cn&expire=%d&ticket=%s", body.app_id.getValue(), qrCodeInfoMap.get(body.device).time, qrCodeInfoMap.get(body.device).id));
        }}));
    }


    // Classes
    public static class FetchModel {
        public ApplicationId app_id;
        public String device;
    }

    @Getter
    public static class QRCodeInfo {
        private final String id;
        private final int time;

        public QRCodeInfo() {
            this.id = EncryptionManager.generateRandomKey(10);
            this.time = 1440;
        }
    }
}