package org.genshinimpact.webserver.routes.mdk;

// Imports
import com.github.benmanes.caffeine.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.WebConfig;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.mdk.agreement.*;
import org.genshinimpact.webserver.responses.mdk.agreement.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/agreement/api", "mdk/agreement/api"}, produces = "application/json")
public final class MDKAgreementController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code MDKAgreementController}.
     * @param requestRateCache The rate limit cache.
     */
    public MDKAgreementController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/agreement/api/getAgreementInfos">https://devapi-takumi.mihoyo.com/mdk/agreement/api/getAgreementInfos</a><br><br>
     *  Description: Fetches the marketing agreements (Available only in overseas version).<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code country_code} — The client's country code.</li>
     *          <li>{@code token} — The client's session token.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *        </ul>
     */
    @GetMapping("getAgreementInfos")
    public ResponseEntity<Response<?>> SendMdkAgreementInfos(HttpServletRequest request, String biz_key, String country_code, String token, String uid) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        if(biz_key == null || biz_key.isBlank() || country_code == null || country_code.isBlank() || token == null || token.isBlank() || uid == null || uid.isBlank()) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AgreementGetAgreementInfos(SpringBootApp.getWebConfig().marketing_agreements)));
        }

        try {
            var myAccount = DBUtils.findAccountById(Long.parseLong(uid));
            if(myAccount == null || !myAccount.getSessionToken().equals(token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            List<WebConfig.AgreementConfig> marketing_agreements = new ArrayList<>();
            for(var info : SpringBootApp.getWebConfig().marketing_agreements) {
                if(!myAccount.getAgreementInfos().containsKey(info.agreement_id))
                    marketing_agreements.add(info);
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AgreementGetAgreementInfos(marketing_agreements)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/agreement/api/operateAgreement">https://devapi-takumi.mihoyo.com/mdk/agreement/api/operateAgreement</a><br><br>
     *  Description: Operates an agreement (Available only in overseas version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code agreement_id} — The agreement's id.</li>
     *          <li>{@code agreement_version} — The agreement's version.</li>
     *          <li>{@code operation} — The operation's name.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *          <li>{@code token} — The client's session token.</li>
     *        </ul>
     */
    @PostMapping("operateAgreement")
    public ResponseEntity<Response<?>> SendMdkOperateAgreement(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        MdkOperateAgreementModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), MdkOperateAgreementModel.class);
            if(body.agreement_id == null || body.agreement_version == null || body.uid == null || body.uid.isBlank() || body.operation == null || body.operation.isBlank() || body.token == null || body.token.isBlank() || (!body.operation.equals("DENY") && !body.operation.equals("ACCEPT"))) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.uid));
            if(myAccount == null || !myAccount.getSessionToken().equals(body.token) || myAccount.getAgreementInfos().containsKey(body.agreement_id)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            myAccount.getAgreementInfos().putIfAbsent(body.agreement_id, body.operation.equals("ACCEPT"));
            myAccount.save(true);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}