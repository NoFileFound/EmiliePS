package org.genshinhttpsrv.controllers.account;

// Imports
import static org.genshinhttpsrv.api.Retcode.RETCODE_SUCC;
import static org.genshinhttpsrv.api.Retcode.RET_HTTP_BAD_REQUEST;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.enums.CaptchaAction;
import org.genshinhttpsrv.api.enums.ClientType;
import org.genshinhttpsrv.api.enums.RegionType;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.genshinhttpsrv.libraries.GeetestLib;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "account/risky/api", produces = "application/json")
public final class Risky implements Response {
    private final String[] ACTIONS = {"login", "device_grant", "bind_mobile", "modify_realname", "bind_email"};

    /**
     *  Source: <a href="https://gameapi-account.mihoyo.com/account/risky/api/check">https://gameapi-account.mihoyo.com/account/risky/api/check</a><br><br>
     *  Description: Determines should the client do a captcha or not.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_type} — The client's action to do.</li>
     *          <li>{@code api_name} — The client's endpoint redirect.</li>
     *          <li>{@code username} — The client's account name. (For login)</li>
     *          <li>{@code mobile} — The client account's phone number (If using mobile endpoint).</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-game_biz} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code x-rpc-client_type} — The client's platform type.</li>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "check")
    public ResponseEntity<LinkedHashMap<String, Object>> SendCheck(@RequestBody RiskyCheckModel body, @RequestHeader(value = "x-rpc-game_biz", required = false) RegionType game_biz, @RequestHeader(value = "x-rpc-client_type", required = false) ClientType client_type, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(body == null || body.action_type == null || body.action_type.isEmpty() || body.api_name == null || body.api_name.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(RET_HTTP_BAD_REQUEST, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(game_biz == null || game_biz == RegionType.REGION_UNKNOWN || client_type == null || client_type == ClientType.PLATFORM_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(RET_HTTP_BAD_REQUEST, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(Stream.of(this.ACTIONS).noneMatch(body.action_type::equals)) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "unknown_risky_action_found", request.getRemoteAddr(), body.action_type));
            return ResponseEntity.ok(this.makeResponse(RET_HTTP_BAD_REQUEST, Application.getTranslationManager().get(lang, "retcode_risky_invalid_request"), null));
        }

        if(Application.getPropertiesInfo().disable_mmt) {
            return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
                put("id", "");
                put("action", CaptchaAction.ACTION_NONE.toString());
                put("geetest", null);
            }}));
        }

        String riskId = EncryptionManager.md5Encode(EncryptionManager.generateRandomKey(32));
        String challengeHash = GeetestLib.generateChallenge();
        Application.getLogger().debug(Application.getTranslationManager().get("console", "risky_new_captcha_generated", request.getRemoteAddr(), riskId));
        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("id", riskId);
            put("action", CaptchaAction.ACTION_GEETEST.toString());
            put("geetest", new LinkedHashMap<>() {{
                put("success", !challengeHash.isEmpty() ? 1 : 0);
                put("gt", Application.getPropertiesInfo().geetest.gt);
                put("challenge", challengeHash);
                put("is_new_captcha", 1);
            }});
        }}));
    }


    // Classes
    public static class RiskyCheckModel {
        public String action_type;
        public String api_name;
        public String username = null;
        public String mobile = null;
    }
}