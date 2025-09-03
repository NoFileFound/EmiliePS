package org.emilieps.webroutes;

// Imports
import static org.emilieps.data.HttpRetcode.RETCODE_FAIL;
import static org.emilieps.data.HttpRetcode.RETCODE_SUCC;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public final class Miscellaneous {
    /**
     *  Source: <a href="https://apm-log-upload.mihoyo.com/_ts">https://apm-log-upload.mihoyo.com/_ts</a><br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br>
     */
    @GetMapping(value = "_ts", produces = "application/json")
    public ResponseEntity<LinkedHashMap<String, Object>> SendTS() {
        return ResponseEntity.ok(new LinkedHashMap<>() {{
            put("code", RETCODE_SUCC);
            put("message", "app running");
            put("milliTs", String.valueOf(System.currentTimeMillis()));
        }});
    }

    /**
     *  Source: <a href="https://apm-log-upload.mihoyo.com/ping">https://apm-log-upload.mihoyo.com/ping</a><br><br>
     *  Method: GET<br>
     */
    @GetMapping(value = "ping")
    public ResponseEntity<String> SendPing() {
        return ResponseEntity.ok("ok");
    }

    /**
     *  Source: <a href="https://sg-public-api.hoyoverse.com/upload/outer/GetStByComboToken">https://sg-public-api.hoyoverse.com/upload/outer/GetStByComboToken</a><br><br>
     *  Description: Creates a cloud storage to upload a crash dump.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz} — The requested security biz name. (game-security-st)</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-combo_token} — The client's combo token info.</li>
     *        </ul>
     *
     */
    @PostMapping(value = "upload/outer/GetStByComboToken")
    public ResponseEntity<LinkedHashMap<String, Object>> SendStByComboToken() {
        return ResponseEntity.ok(new LinkedHashMap<>() {{
            put("retcode", RETCODE_FAIL);
            put("message", "Unavailable");
            put("data", null);
        }});
    }

    /**
     *  Source: <a href="https://hk4e-beta-sdk-os.hoyoverse.com/dispatch/dispatch/getGateAddress">https://hk4e-beta-sdk-os.hoyoverse.com/dispatch/dispatch/getGateAddress</a><br><br>
     *  Description: Sends the list of game server ip addresses and their ports.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code region} — The requested region name.</li>
     *        </ul>
     *
     */
    @GetMapping(value = "dispatch/dispatch/getGateAddress")
    public ResponseEntity<LinkedHashMap<String, Object>> SendGateAddress() {
        return ResponseEntity.ok(new LinkedHashMap<>() {{
            put("retcode", RETCODE_SUCC);
            put("message", "OK");
            put("data", new LinkedHashMap<>() {{
                put("address_list", new ArrayList<>());
            }});
        }});
    }
}