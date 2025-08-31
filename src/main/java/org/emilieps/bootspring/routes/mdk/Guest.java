package org.emilieps.bootspring.routes.mdk;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.bootspring.data.HttpRetcode;
import org.emilieps.bootspring.data.Response;
import org.emilieps.data.enums.ClientType;
import org.emilieps.data.enums.RegionType;
import org.emilieps.database.DBManager;
import org.emilieps.database.DBUtils;
import org.emilieps.database.collections.Account;
import org.emilieps.libraries.EncryptionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/guest/guest","hk4e_cn/mdk/guest/guest","mdk/guest/guest"}, produces = "application/json")
public final class Guest implements Response {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/guest/guest/v2/login">https://devapi-takumi.mihoyo.com/mdk/guest/guest/v2/login</a><br><br>
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
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslationManager().get(lang, "retcode_guest_disabled"), null));
        }

        if(body.game_key == null || body.game_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_invalid_game_biz"), null));
        }

        if(body.client == null || body.client == ClientType.PLATFORM_UNKNOWN || body.client == ClientType.PLATFORM_WEB || body.client == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        String hmacSign = EncryptionManager.generateMDKSignature(String.format("%s%s%s", body.client.getValue(), body.device, body.game_key.getValue()), !request.getRequestURL().toString().contains("hk4e_cn"));
        if(hmacSign == null || !hmacSign.equals(body.sign)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslationManager().get(lang, "retcode_signature_error"), null));
        }

        if(DBManager.getDataStore().getDatabase().getCollection("guests").countDocuments() > Application.getPropertiesInfo().maximum_guests) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslationManager().get(lang, "retcode_maximum_guests_reached"), null));
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
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", responseData));
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