package org.genshinhttpsrv.controllers.mdk;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.api.enums.ClientType;
import org.genshinhttpsrv.api.enums.RegionType;
import org.genshinhttpsrv.database.DBManager;
import org.genshinhttpsrv.database.DBUtils;
import org.genshinhttpsrv.database.collections.Account;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/guest/guest","hk4e_cn/mdk/guest/guest","mdk/guest/guest"}, produces = "application/json")
public final class Guest implements Response {
    /**
     *  Source: <a href="https://hk4e-sdk.mihoyo.com/mdk/guest/guest/v2/login">https://hk4e-sdk.mihoyo.com/mdk/guest/guest/v2/login</a><br><br>
     *  Description: Processes a guest login.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code device} — The client's device id.</li>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code sign} — The HMAC signature to check..</li>
     *          <li>{@code g_version} — The game's version name.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = {"login", "v2/login"})
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogin(@RequestBody GuestLoginModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        if(!Application.getPropertiesInfo().enable_guest_login) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslationManager().get(lang, "retcode_guest_disabled"), null));
        }

        if(body.game_key == null || body.game_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_invalid_game_biz"), null));
        }

        if(body.client == null || body.client == ClientType.PLATFORM_UNKNOWN || body.client == ClientType.PLATFORM_WEB || body.client == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        String hmacSign = EncryptionManager.generateHMAC(String.format("game_key=%s&client=%s&device=%s", body.game_key.getValue(), body.client.getValue(), body.device), !request.getRequestURL().toString().contains("hk4e_cn"));
        if(hmacSign == null || !hmacSign.equals(body.sign)) {
            /// TODO: return ResponseEntity.ok(this.makeResponse(Retcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get("retcode_signature_error"), null));
        }

        if(DBManager.getDataStore().getDatabase().getCollection("guests").countDocuments() > Application.getPropertiesInfo().maximum_guests) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslationManager().get(lang, "retcode_maximum_guests_reached"), null));
        }

        var account = DBUtils.findGuestById(body.device);
        var ipAddress = request.getRemoteAddr();
        var isNewly = (account == null);
        if(isNewly) {
            account = new Account("", "", ipAddress, "JP", true, body.device);
            Application.getLogger().info(Application.getTranslationManager().get("console", "new_guestuser_created", account.get_id(), ipAddress));
        } else {
            account.generateGameToken();
            if(!account.getIpAddress().equals(ipAddress)) {
                account.setIpAddress(ipAddress);
                account.save();
            }
        }

        DBUtils.getCachedAccountDevices().putIfAbsent(body.device, account);
        LinkedHashMap<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("guest_id", account.get_id());
        responseData.put("newly", isNewly);
        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", responseData));
    }


    // Classes
    public static class GuestLoginModel {
        public RegionType game_key;
        public ClientType client;
        public String device;
        public String sign;
        public String g_version;
    }
}