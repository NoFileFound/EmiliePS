package org.genshinimpact.webserver.routes.combo;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.panda.*;
import org.genshinimpact.webserver.responses.PandaQRCodeResponse;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.stores.PandaQRCodesStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/combo/panda/qrcode", "hk4e_cn/combo/panda/qrcode", "combo/panda/qrcode"}, produces = "application/json")
public final class ComboPandaController {
    private final PandaQRCodesStore pandaQRCodesStore;

    /**
     * Creates a new {@code ComboPandaController}.
     * @param pandaQRCodesStore The qrcode storage component used to persist incoming QRCode requests.
     */
    public ComboPandaController(PandaQRCodesStore pandaQRCodesStore) {
        this.pandaQRCodesStore = pandaQRCodesStore;
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/confirm">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/confirm</a><br><br>
     *  Description: Confirms a scanned QR code.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qrcode ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *          <li>{@code payload} — The fetched payload from the scan.</li>
     *        </ul>
     */
    @PostMapping(value = "confirm")
    public ResponseEntity<Response<?>> SendPandaConfirm() {
        ///  TODO: IMPLEMENT THIS ENDPOINT: https://devapi-takumi.mihoyo.com/combo/panda/qrcode/confirm
        return null;
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/fetch">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/fetch</a><br><br>
     *  Description: Generates a new QR code for login.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *        </ul>
     */
    @PostMapping(value = "fetch")
    public ResponseEntity<Response<?>> SendPandaFetch(HttpServletRequest request, @RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz) {
        PandaFetchModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), PandaFetchModel.class);
            if(body == null || body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.device == null || body.device.isBlank() || game_biz == null || game_biz.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            }

            if(AppName.fromValue(game_biz) == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            }
        } catch (Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
        }

        if(!SpringBootApp.getWebConfig().mdkConfig.enable_qrcode_login) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "二维码功能已关闭"));
        }

        var myQrCode = this.pandaQRCodesStore.generateQRCode(body.device);
        if(myQrCode == null) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "系统请求失败，请返回重试"));
        }

        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", Map.of("url", String.format("https://user.mihoyo.com/qr_code_in_game.html?app_id=%d&app_name=%s&bbs=false&biz_key=%s&expire=%d&ticket=%s", body.app_id.getValue(), "原神", game_biz, myQrCode.getTime(), myQrCode.getId()))));
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/query">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/query</a><br><br>
     *  Description: Queries the current status of a previously generated QR code for login.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qr code ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     */
    @PostMapping(value = "query")
    public ResponseEntity<Response<?>> SendPandaQuery(HttpServletRequest request) {
        PandaQueryModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), PandaQueryModel.class);
            if(body == null || body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.device == null || body.device.isBlank() || body.ticket == null || body.ticket.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            }
        }catch (Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
        }

        if(!SpringBootApp.getWebConfig().mdkConfig.enable_qrcode_login) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "二维码功能已关闭"));
        }

        int status = this.pandaQRCodesStore.getQrCodeStatus(body.ticket);
        return switch(status) {
            case -1000 -> // The QR code does not exist.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            case -1001 -> // The QR code has expired.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "二维码已过期，请重新生成二维码"));
            case -1002 -> // The QR code was scanned.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new PandaQRCodeResponse("Scanned")));
            case -1003 -> // The QR code was confirmed.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new PandaQRCodeResponse("Confirmed", this.pandaQRCodesStore.getRawPayload(body.ticket))));
            default ->
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new PandaQRCodeResponse("Init")));
        };
    }

    /**
     * Source: <a href="https://devapi-takumi.mihoyo.com/combo/panda/qrcode/scan">https://devapi-takumi.mihoyo.com/combo/panda/qrcode/scan</a><br><br>
     *  Description: Notifies that the user has scanned the QR code.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code ticket} — The client's qr code ticket id.</li>
     *          <li>{@code device} — The client's device id.</li>
     *        </ul>
     */
    @PostMapping(value = "scan")
    public ResponseEntity<Response<?>> SendPandaScan(HttpServletRequest request) {
        PandaScanModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), PandaScanModel.class);
            if(body == null || body.app_id == null || body.app_id == AppId.APP_UNKNOWN || body.device == null || body.device.isBlank() || body.ticket == null || body.ticket.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            }
        }catch (Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
        }

        if(!SpringBootApp.getWebConfig().mdkConfig.enable_qrcode_login) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "二维码功能已关闭"));
        }

        int status = this.pandaQRCodesStore.getQrCodeStatus(body.ticket);
        return switch(status) {
            case -1000 -> // The QR code does not exist.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "系统请求失败，请返回重试"));
            case -1001 -> // The QR code has expired.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "二维码已过期，请重新生成二维码"));
            case -1002 -> // The QR code was already scanned.
                    ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "二维码已失效"));
            default -> {
                this.pandaQRCodesStore.setScannedQrCode(body.ticket);
                yield ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
            }
        };
    }
}