package org.genshinhttpsrv.controllers.devicefp;

// Imports
import static org.genshinhttpsrv.api.Retcode.RETCODE_SUCC;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.enums.ClientType;
import org.genshinhttpsrv.api.enums.RegionType;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "device-fp/api", produces = "application/json")
public final class DeviceFp implements Response {
    /**
     *  Source: <a href="https://public-data-api.mihoyo.com/device-fp/api/getExtList">https://public-data-api.mihoyo.com/device-fp/api/getExtList</a><br><br>
     *  Description: Fetches information about the device extensions and package string.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code platform} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping("getExtList")
    public ResponseEntity<LinkedHashMap<String, Object>> SendExtList(ClientType platform) {
        if(platform == null) {
            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("code", 401);
                put("msg", "传入的参数有误");
                put("ext_list", List.of());
                put("pkg_list", List.of());
                put("pkg_str", "");
            }}));
        }

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        if(platform == ClientType.PLATFORM_UNKNOWN) {
            data.put("code", 401);
            data.put("msg", "不支持的platform");
            data.put("ext_list", List.of());
            data.put("pkg_list", List.of());
            data.put("pkg_str", "");
        } else {
            var extInfo = Application.getDeviceExtensionsInfo().get(platform.getValue());
            data.put("code", 200);
            data.put("msg", "ok");
            data.put("ext_list", (extInfo == null ? List.of() : extInfo.ext_list));
            data.put("pkg_list", (extInfo == null ? List.of() : extInfo.pkg_list));
            data.put("pkg_str", (extInfo == null ? "" : extInfo.pkg_str));
        }

        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", data));
    }

    /**
     * Source: <a href="https://public-data-api.mihoyo.com/device-fp/api/getFp">https://public-data-api.mihoyo.com/device-fp/api/getFp</a><br><br>
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
     *          <li>{@code app_name} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code platform} — The client's device extensions to provide.</li>
     *        </ul>
     */
    @PostMapping("getFp")
    public ResponseEntity<LinkedHashMap<String, Object>> SendFp(@RequestBody GetFpModel body) {
        if(body.platform == null || body.platform == ClientType.PLATFORM_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("device_fp", body.device_fp);
                put("code", 401);
                put("msg", "不支持的platform");
            }}));
        }

        if (Stream.of(
                Optional.ofNullable(body.device_id).filter(s -> !s.isBlank()),
                Optional.ofNullable(body.seed_id).filter(s -> !s.isBlank()),
                Optional.ofNullable(body.seed_time).filter(s -> !s.isBlank()).filter(seedTime -> {
                    try {
                        long _seedTime = Long.parseLong(seedTime);
                        return _seedTime >= 1000000000000L && _seedTime <= 9999999999999L;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }),
                Optional.ofNullable(body.device_fp).filter(s -> !s.isBlank()).filter(s -> s.length() == 13),
                Optional.ofNullable(body.app_name).filter(app -> app != RegionType.REGION_UNKNOWN),
                Optional.ofNullable(body.ext_fields).filter(s -> {
                    try {
                        var jsonMap = JsonLoader.parseValue(s, Map.class);
                        var extInfo = Application.getDeviceExtensionsInfo().get(body.platform.getValue());
                        var requiredExts = (extInfo != null) ? extInfo.ext_list : List.of();
                        return requiredExts.stream().allMatch(key -> jsonMap.containsKey(key) || key.equals("oaid") || key.equals("vaid") || key.equals("aaid"));
                    } catch (Exception e) {
                        return false;
                    }
                })
        ).anyMatch(Optional::isEmpty)) {
            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("device_fp", body.device_fp);
                put("code", 403);
                put("msg", "传入的参数有误");
            }}));
        }

        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("device_fp", body.device_fp);
            put("code", 200);
            put("msg", "ok");
        }}));
    }


    // Classes
    public static class GetFpModel {
        public String device_id;
        public String seed_id;
        public String seed_time;
        public ClientType platform;
        public String device_fp;
        public RegionType app_name;
        public String ext_fields;
    }
}