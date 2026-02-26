package org.genshinimpact.webserver.routes.mdk;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.mdk.guest.*;
import org.genshinimpact.webserver.responses.mdk.guest.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/guest/guest","hk4e_cn/mdk/guest/guest","mdk/guest/guest"}, produces = "application/json")
public final class MDKGuestController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code MDKGuestController}.
     * @param requestRateCache The rate limit cache.
     */
    public MDKGuestController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/guest/guest/v2/login">https://devapi-takumi.mihoyo.com/mdk/guest/guest/v2/login</a><br><br>
     *  Description: Processes a guest login.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_key} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code device} — The client's device id.</li>
     *          <li>{@code client} — The client's platform type.</li>
     *          <li>{@code sign} — The HMAC signature to check.</li>
     *          <li>{@code g_version} — The game's version name.</li>
     *        </ul>
     */
    @PostMapping(value = {"login", "v2/login"})
    public ResponseEntity<Response<?>> SendGuestLogin(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        GuestLoginModel body;
        try {
            body = JsonUtils.read(request.getInputStream(),GuestLoginModel.class);
            if(body.game_key == null || body.game_key == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "游戏不存在"));
            }

            if(body.client == null || body.client == ClientType.PLATFORM_UNKNOWN || body.device == null || body.device.isBlank() || body.sign == null || body.sign.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(!SpringBootApp.getWebConfig().mdkConfig.enable_guest) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_GUEST_LOGIN_ERROR, "客人已禁用"));
            }

            if(DBManager.getDataStore().getDatabase().getCollection("guests").countDocuments() > AppBootstrap.getMainConfig().maximumGuests) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_GUEST_LOGIN_ERROR, "快速游戏人数已满"));
            }

            String hmacSign = CryptoUtils.getHMAC256(String.format("%s%s%s", body.client.getValue(), body.device, body.game_key.getValue()), (body.game_key == AppName.APP_GENSHIN ? CryptoUtils.getMdkKeys().get(1) : CryptoUtils.getMdkKeys().get(3)));
            if(!hmacSign.equals(body.sign)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "签名错误"));
            }

            var myGuest = DBUtils.getOrCreateGuest(body.device);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new GuestLoginResponse(myGuest.getId(), myGuest.getIsNew())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}