package org.genshinimpact.webserver.routes.account;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_RATE_LIMIT_EXCEEDED;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_PARAMETER_ERROR;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.webserver.stores.GeetestStore;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.ClientApiActionType;
import org.genshinimpact.webserver.models.account.risky.*;
import org.genshinimpact.webserver.responses.account.risky.*;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "account/risky/api", produces = "application/json")
public final class AccountRiskyController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code AccountRiskyController}.
     * @param requestRateCache The rate limit cache.
     */
    public AccountRiskyController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/risky/api/check">https://devapi-takumi.mihoyo.com/account/risky/api/check</a><br><br>
     *  Description: Determines whether the client should perform a captcha.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_type} — The client's action type.</li>
     *          <li>{@code api_name} — The client's action redirect endpoint.</li>
     *          <li>{@code username} — The client's account name.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn})</li>
     *          <li>{@code x-rpc-client_type} — The client's platform type.</li>
     *        </ul>
     */
    @PostMapping(value = "check")
    public ResponseEntity<Response<?>> SendRiskyCheck(HttpServletRequest request, @RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz, @RequestHeader(value = "x-rpc-client_type", required = false) String client_type) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 15) {
            return ResponseEntity.ok(new Response<>(RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        RiskyCheckModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), RiskyCheckModel.class);
            AppName appName = AppName.fromValue(game_biz);
            ClientType clientType = ClientType.fromValue(client_type);
            if(body == null || body.action_type == null || body.action_type == ClientApiActionType.CLIENT_API_ACTION_TYPE_UNKNOWN || body.api_name == null || body.api_name.isBlank() || (body.username.isBlank() && body.mobile.isBlank() && body.action_type == ClientApiActionType.CLIENT_API_ACTION_TYPE_LOGIN) || appName == AppName.APP_UNKNOWN || clientType == ClientType.PLATFORM_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(RETCODE_PARAMETER_ERROR, "您的请求存在安全风险"));
            }
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(RETCODE_PARAMETER_ERROR, "您的请求存在安全风险"));
        }

        if(!SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new RiskyCheckResponse()));
        }

        GeetestStore.GeetestClient geetestClient = SpringBootApp.getCaptchaStore().getOrGenerateCaptcha(ipAddress);
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new RiskyCheckResponse(geetestClient.getRiskId(), geetestClient.getModel())));
    }
}