package org.genshinhttpsrv.controllers;

// Imports
import java.util.LinkedHashMap;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.database.DBUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "perf/config", produces = "application/json")
public final class Perf implements Response {
    @Override public LinkedHashMap<String, Object> makeResponse(Retcode retcode, String message, Object data) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        response.put("retcode", retcode);
        response.put("message", message);

        return response;
    }

    /**
     *  Source: <a href="https://log-upload.mihoyo.com/perf/config/verify">https://log-upload.mihoyo.com/perf/config/verify</a><br><br>
     *  Description: Verifies the client about sending perf logs.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code device_id} — The client's device id.</li>
     *          <li>{@code platform} — The client's platform type.</li>
     *          <li>{@code name} — The client's desktop name.</li>
     *        </ul>
     */
    @GetMapping(value = "verify")
    public ResponseEntity<LinkedHashMap<String, Object>> SendVerify(@RequestParam String device_id, @RequestParam String platform, @RequestParam String name) {
        if(device_id == null || device_id.isEmpty() || platform == null || platform.isEmpty() || name == null || name.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PERF_VERIFY_FAILED, "校验失败", null));
        }

        var userObj = DBUtils.getCachedAccountDevices().get(device_id);
        if(userObj == null || (!name.contains("DESKTOP") && platform.equals("2"))) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PERF_VERIFY_MISSHIT, "未命中", null));
        }

        if(!userObj.getApprovedDevices().contains(device_id)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_FAIL, "not matched", null));
        }

        userObj.setDesktopName(name);
        userObj.save();
        DBUtils.getCachedAccountDevices().remove(device_id);
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "ok", null));
    }
}