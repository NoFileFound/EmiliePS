package org.genshinimpact.webserver.routes.account;

// Imports
import static org.genshinimpact.webserver.utils.Utils.filterString;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Ticket;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.account.auth.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.responses.account.auth.AuthBindMobileResponse;
import org.genshinimpact.webserver.responses.account.auth.AuthBindRealNameResponse;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "account/auth/api", produces = "application/json")
public final class AccountAuthController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code AccountAuthController}.
     * @param requestRateCache The rate limit cache.
     */
    public AccountAuthController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/auth/api/bindMobile">https://devapi-takumi.mihoyo.com/account/auth/api/bindMobile</a><br><br>
     *  Description: Binds the phone number to the account.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code area_code} — The mobile number's area code.</li>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code mobile} — The mobile number.</li>
     *          <li>{@code captcha} — The client's verification code.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *        </ul>
     */
    @PostMapping(value = "bindMobile")
    public ResponseEntity<Response<?>> SendAuthBindMobile(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        AuthBindMobileModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), AuthBindMobileModel.class);
            if(body == null || body.ticket == null || body.ticket.isBlank() || body.mobile == null || body.mobile.isBlank() || body.uid == null || body.uid.isBlank() || body.area_code == null || body.area_code.isBlank() || body.captcha == null || body.captcha.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(!body.area_code.equals("+86") || !(body.area_code + body.mobile).matches("^(?:\\+?86)?1(?:3\\d{3}|5[^4\\D]\\d{2}|8\\d{3}|7(?:[0-35-9]\\d{2}|4(?:0\\d|1[0-2]|9\\d))|9[0-35-9]\\d{2}|6[2567]\\d{2}|4(?:(?:10|4[01])\\d{3}|[68]\\d{4}|[579]\\d{2}))\\d{6}$")) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "请输入正确的手机号码"));
            }

            var myTicket = DBUtils.getTicketById(body.ticket);
            if(myTicket == null || !myTicket.getType().equals(Ticket.TicketType.TICKET_BIND_MOBILE)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null || !body.uid.equals(String.valueOf(myAccount.getId()))) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(!myTicket.getVerCode().equals(body.captcha)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "验证码错误"));
            }

            myAccount.setMobileNumber(body.mobile);
            SpringBootApp.getTicketStore().removeTicket(myTicket, myAccount);
            myAccount.save(true);
            AppBootstrap.getLogger().info("[Mobile Binding] The mobile binding ended successfully on account: {}.", myAccount.getId());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AuthBindMobileResponse(Long.parseLong(body.uid), myAccount.getUsername(), filterString(myAccount.getEmailAddress()), body.mobile, body.area_code)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/auth/api/bindRealname">https://devapi-takumi.mihoyo.com/account/auth/api/bindRealname</a><br><br>
     *  Description: Binds the real name and identity card to the account.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code realname} — The real name.</li>
     *          <li>{@code identity} — The identity card.</li>
     *          <li>{@code is_crypto} — Are the real name and identity card encrypted.</li>
     *        </ul>
     */
    @PostMapping(value = {"bindRealname", "modifyRealname"})
    public ResponseEntity<Response<?>> SendAuthBindRealName(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        AuthBindRealNameModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), AuthBindRealNameModel.class);
            if(body == null || body.is_crypto == null || body.ticket == null || body.ticket.isBlank() || body.identity == null || body.identity.isBlank() || body.realname == null || body.realname.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myTicket = DBUtils.getTicketById(body.ticket);
            if(myTicket == null || !myTicket.getType().equals(Ticket.TicketType.TICKET_BIND_REALNAME)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            String realNameDec = body.realname;
            String identityDec = body.identity;
            if(body.is_crypto) {
                realNameDec = CryptoUtils.decryptPassword(realNameDec);
                identityDec = CryptoUtils.decryptPassword(identityDec);
                if(realNameDec.isEmpty() || identityDec.isEmpty()) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SYSTEM_ERROR, "密码解密错误"));
                }
            }

            myAccount.setIdentityName(realNameDec);
            myAccount.setIdentityCard(identityDec);
            SpringBootApp.getTicketStore().removeTicket(myTicket, myAccount);
            myAccount.save(true);
            AppBootstrap.getLogger().info("[Realname Binding] The Realname binding ended successfully on account: {}.", myAccount.getId());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AuthBindRealNameResponse(myAccount.getId(), filterString(myAccount.getUsername()), filterString(myAccount.getEmailAddress()), body.identity, body.realname)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}