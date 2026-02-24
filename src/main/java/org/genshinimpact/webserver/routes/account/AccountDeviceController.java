package org.genshinimpact.webserver.routes.account;

// Imports
import static org.genshinimpact.database.collections.Ticket.TicketType.TICKET_DEVICE_GRANT;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.embeds.DeviceInfo;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.device.*;
import org.genshinimpact.webserver.responses.DeviceGrantResponse;
import org.genshinimpact.webserver.responses.DevicePreGrantByTicketResponse;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "account/device/api", produces = "application/json")
public final class AccountDeviceController {
    private final Cache<String, AtomicInteger> requestRateCache;

    /**
     * Creates a new {@code AccountDeviceController}.
     * @param requestRateCache The rate limit cache.
     */
    public AccountDeviceController(Cache<String, AtomicInteger> requestRateCache) {
        this.requestRateCache = requestRateCache;
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/preGrantByTicket">https://devapi-takumi.mihoyo.com/account/device/api/preGrantByTicket</a><br><br>
     *  Description: Sends email or SMS notification about new device entrance with a code.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code action_ticket} — The ticket id.</li>
     *          <li>{@code device} — Information about the client's current device. (type, name, id and model).</li>
     *          <li>{@code way} — The way of sending the notification (SMS/Email).</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-risky} — The verification token after captcha.</li>
     *        </ul>
     */
    @PostMapping(value = "preGrantByTicket")
    public ResponseEntity<Response<?>> SendDevicePreGrantByTicket(HttpServletRequest request, @RequestHeader(value = "x-rpc-risky", required = false) String risky) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        DevicePreGrantByTicketModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), DevicePreGrantByTicketModel.class);
            if(body == null || body.device == null || body.device.device_id == null || body.device.device_model == null || body.device.device_name == null || body.device.client == null || body.device.client == ClientType.PLATFORM_UNKNOWN || body.action_ticket == null || body.action_ticket.isBlank() || body.way == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            if(SpringBootApp.getWebConfig().mdkConfig.enable_mtt) {
                if(SpringBootApp.getCaptchaStore().checkCaptchaStatus(risky)) {
                    return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
                }

                SpringBootApp.getCaptchaStore().deleteCaptcha(ipAddress);
            }

            var myTicket = DBUtils.getTicketById(body.action_ticket);
            if(myTicket == null || !myTicket.getType().equals(TICKET_DEVICE_GRANT)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null || !myAccount.getRequireDeviceGrantTicket().equals(body.action_ticket)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            switch(body.way) {
                case Way_Email -> {
                    ///  TODO: IMPLEMENT EMAIL SUPPORT
                    break;
                }
                case Way_BindMobile -> {
                    ///  TODO: IMPLEMENT MOBILE SUPPORT
                    break;
                }
                case Way_SafeMobile -> {
                    ///  TODO: IMPLEMENT MOBILE SUPPORT.
                    break;
                }
            }

            myTicket.setVerCode("%06d".formatted(new java.security.SecureRandom().nextInt(1000000)));
            myTicket.setData(body.device);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new DevicePreGrantByTicketResponse(body.action_ticket)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/grant">https://devapi-takumi.mihoyo.com/account/device/api/grant</a><br><br>
     *  Description: Verifies the verification code for device grant.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code ticket} — The ticket id.</li>
     *          <li>{@code code} — The verification code.</li>
     *        </ul>
     */
    @PostMapping(value = "grant")
    public ResponseEntity<Response<?>> SendDeviceGrant(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        DeviceGrant body;
        try {
            body = JsonUtils.read(request.getInputStream(), DeviceGrant.class);
            if(body == null || body.ticket == null || body.ticket.isBlank() || body.code == null || body.code.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
            }

            var myTicket = DBUtils.getTicketById(body.ticket);
            if(myTicket == null || !myTicket.getType().equals(TICKET_DEVICE_GRANT)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(myTicket.isExpired()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_REQUEST_FAILED, "票据已过期，请重新登录"));
            }

            var myAccount = DBUtils.findAccountById(myTicket.getAccountId());
            if(myAccount == null || !myAccount.getRequireDeviceGrantTicket().equals(body.ticket)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_NETWORK_AT_RISK, "请求失败，当前网络环境存在风险"));
            }

            if(!myTicket.getVerCode().equals(body.code)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_CONFIGURATION_ERROR, "验证码错误"));
            }

            var myDeviceModel = (DeviceInfoModel)myTicket.getData();
            myAccount.getDeviceInfo().put(myDeviceModel.device_id, new DeviceInfo(myDeviceModel));
            myAccount.setSessionToken(CryptoUtils.generateStringKey(32));
            SpringBootApp.getTicketStore().removeTicket(myTicket, myAccount);
            myAccount.save(true);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new DeviceGrantResponse("", myAccount.getSessionToken())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}