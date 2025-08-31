package org.emilieps.bootspring.routes;

// Imports
import static org.emilieps.bootspring.data.HttpRetcode.RETCODE_SUCC;
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
}