package org.emilieps.webroutes.mdk;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.data.HttpRetcode;
import org.emilieps.data.webserver.Response;
import org.emilieps.data.enums.ClientType;
import org.emilieps.data.enums.RegionType;
import org.emilieps.database.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.GeoIPLib;
import org.emilieps.library.MongodbLib;

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
        if(!Application.getHttpConfig().enable_guest_login) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslations().get(lang, "retcode_guest_disabled"), null));
        }

        if(body.game_key == null || body.game_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_invalid_game_biz"), null));
        }

        if(body.client == null || body.client == ClientType.PLATFORM_UNKNOWN || body.client == ClientType.PLATFORM_WEB || body.client == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        String hmacSign = EncryptionLib.generateMDKSignature(String.format("%s%s%s", body.client.getValue(), body.device, body.game_key.getValue()), !request.getRequestURL().toString().contains("hk4e_cn"));
        if(hmacSign == null || !hmacSign.equals(body.sign)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslations().get(lang, "retcode_signature_error"), null));
        }

        if(MongodbLib.getDocumentsCount("guests") > Application.getHttpConfig().maximum_guests) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_MAXIMUM_GUESTS_REACHED, Application.getTranslations().get(lang, "retcode_maximum_guests_reached"), null));
        }

        var account = MongodbLib.findGuestById(body.device);
        var ipAddress = request.getRemoteAddr();
        var isNewly = (account == null);
        if(isNewly) {
            account = new Account("", "", ipAddress, GeoIPLib.getCountryCode(ipAddress), true, body.device, body.client.getValue());
            Application.getLogger().info(Application.getTranslations().get("console", "new_guestuser_created", account.get_id(), ipAddress));
        } else {
            account.generateGameToken();
            if(!account.getIpAddress().equals(ipAddress)) {
                account.setIpAddress(ipAddress);
                account.save();
            }
        }

        MongodbLib.getCachedAccountDevices().putIfAbsent(body.device, account);
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