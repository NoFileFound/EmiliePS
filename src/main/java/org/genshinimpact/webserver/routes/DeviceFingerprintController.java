package org.genshinimpact.webserver.routes;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.models.GetFpModel;
import org.genshinimpact.webserver.responses.GetExtListResponse;
import org.genshinimpact.webserver.responses.GetFpResponse;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "device-fp/api", produces = "application/json")
public final class DeviceFingerprintController {
    /**
     *  Source: <a href="https://devop-data-takumi.mihoyo.com/device-fp/api/getExtList">https://devop-data-takumi.mihoyo.com/device-fp/api/getExtList</a><br><br>
     *  Description: Fetches information about the device extensions and package string.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code platform} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping("getExtList")
    public ResponseEntity<Response<?>> SendExtList(@RequestParam(required = false) String platform) {
        if(platform == null || platform.isBlank()) {
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetExtListResponse(403, "传入的参数有误")));
        }

        ClientType clientType = ClientType.fromValue(platform);
        if(clientType == ClientType.PLATFORM_UNKNOWN) {
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetExtListResponse(401, "不支持的platform")));
        }

        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetExtListResponse(200, "ok", SpringBootApp.getWebConfig().extensionList.get(clientType), List.of(), "")));
    }

    /**
     * Source: <a href="https://devop-data-takumi.mihoyo.com/device-fp/api/getFp">https://devop-data-takumi.mihoyo.com/device-fp/api/getFp</a><br><br>
     *  Description: Checks if the device fingerprint is valid and extensions are given.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code device_id} — The client's device id.</li>
     *          <li>{@code seed_id} — The client's seed id.</li>
     *          <li>{@code seed_time} — Timestamp of the generated seed.</li>
     *          <li>{@code platform} — The client's platform type.</li>
     *          <li>{@code platform} — The client's device fingerprint.</li>
     *          <li>{@code app_name} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code platform} — The client's device extensions to provide.</li>
     *        </ul>
     */
    @PostMapping("getFp")
    public ResponseEntity<Response<?>> sendFp(HttpServletRequest request) {
        GetFpModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), GetFpModel.class);
            if(body.platform == null || body.platform == ClientType.PLATFORM_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(401, "不支持的platform")));
            }

            if(Stream.of(body.device_id, body.seed_id, body.seed_time, body.device_fp, body.ext_fields).anyMatch(s -> s == null || s.isBlank()) || body.device_fp.length() < 10 || body.app_name == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(403, "传入的参数有误")));
            }

            try {
                long seedTime = Long.parseLong(body.seed_time);
                if(seedTime < 1000000000000L || seedTime > 9999999999999L) {
                    return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(403, "传入的参数有误")));
                }
            } catch(NumberFormatException e) {
                return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(403, "传入的参数有误")));
            }
        } catch(Exception ex) {
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(403, "传入的参数有误")));
        }

        boolean isValid = false;
        try {
            var jsonMap = JsonUtils.read(body.ext_fields, Map.class);
            var extInfo = SpringBootApp.getWebConfig().extensionList.get(body.platform);
            isValid = extInfo.stream().allMatch(key -> jsonMap.containsKey(key) || key.equals("oaid") || key.equals("vaid") || key.equals("aaid"));
        } catch(Exception ignored) {}

        if(!isValid) {
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(403, "传入的参数有误")));
        }

        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new GetFpResponse(200, "ok", body.device_fp)));
    }
}