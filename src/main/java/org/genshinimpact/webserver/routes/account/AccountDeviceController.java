package org.genshinimpact.webserver.routes.account;

// Imports
import static org.genshinimpact.database.collections.Ticket.TicketType.TICKET_DEVICE_GRANT;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.embeds.DeviceInfo;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.account.device.*;
import org.genshinimpact.webserver.responses.account.device.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.utils.SMSUtils;
import org.genshinimpact.webserver.utils.SMTPUtils;
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

        DeviceGrantModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), DeviceGrantModel.class);
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
            AppBootstrap.getLogger().info("[Binding] The device verification ended successfully on account: {}.", myAccount.getId());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new DeviceGrantResponse("", myAccount.getSessionToken())));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/listNewerDevices">https://devapi-takumi.mihoyo.com/account/device/api/listNewerDevices</a><br><br>
     *  Description: Fetches the device list.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_token} — The client's session token.</li>
     *          <li>{@code account_id} — The client's account id.</li>
     *        </ul>
     */
    @PostMapping(value = "listNewerDevices")
    public ResponseEntity<Response<?>> SendDeviceListNewerDevices(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        DeviceListNewerDevicesModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), DeviceListNewerDevicesModel.class);
            if(body == null || body.account_id == null || body.account_id.isBlank() || body.game_token == null || body.game_token.isBlank()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.account_id));
            if(myAccount == null || !myAccount.getSessionToken().equals(body.game_token)) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
            }

            Map<String, DeviceInfo> devices = new HashMap<>();
            for(var info : myAccount.getDeviceInfo().values()) {
                if(!info.getConfirmed()) {
                    devices.put(info.getDeviceId(), info);
                }
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new DeviceListNewerDevicesResponse(devices)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/account/device/api/ackNewerDevices">https://devapi-takumi.mihoyo.com/account/device/api/ackNewerDevices</a><br><br>
     *  Description: Acknowledges and confirms the new device.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_token} — The client's session token.</li>
     *          <li>{@code account_id} — The client's account id.</li>
     *          <li>{@code latest_id} — The client's latest id of the device's id.</li>
     *        </ul>
     */
    @PostMapping(value = "ackNewerDevices")
    public ResponseEntity<Response<?>> SendDeviceAckNewerDevices(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AtomicInteger counter = this.requestRateCache.get(ipAddress, k -> new AtomicInteger(0));
        if(counter.incrementAndGet() > 20) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_RATE_LIMIT_EXCEEDED, "操作次數過多，請稍後再試"));
        }

        DeviceAckNewerDevicesModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), DeviceAckNewerDevicesModel.class);
            if(body == null || body.account_id == null || body.account_id.isBlank() || body.game_token == null || body.game_token.isBlank() || body.latest_id == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
            }

            var myAccount = DBUtils.findAccountById(Long.parseLong(body.account_id));
            if(myAccount == null || !myAccount.getSessionToken().equals(body.game_token) || body.latest_id > myAccount.getDeviceInfo().size()) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
            }

            int i = 0;
            var devices = myAccount.getDeviceInfo();
            for(var entry : devices.entrySet()) {
                if(i == body.latest_id) {
                    DeviceInfo info = entry.getValue();
                    info.setConfirmed(true);
                    devices.put(entry.getKey(), info);
                    break;
                }

                i++;
            }

            myAccount.setDeviceInfo(devices);
            myAccount.save(true);
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK"));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_ACCOUNT_ERROR, "账号错误"));
        }
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

            String verCode = "%06d".formatted(new java.security.SecureRandom().nextInt(1000000));
            myTicket.setVerCode(verCode);
            myTicket.setData(body.device);
            switch(body.way) {
                case Way_Email -> SMTPUtils.sendDeviceVerificationEmailMessage(myAccount.getEmailAddress(), verCode);
                case Way_BindMobile -> SMSUtils.sendSMS(myAccount.getMobileNumber(), verCode);
                case Way_SafeMobile -> SMSUtils.sendSMS(myAccount.getSafeMobileNumber(), verCode);
            }

            AppBootstrap.getLogger().info("[Binding] The device verification started on account: {} | with verification code: {}.", myAccount.getId(), myTicket.getVerCode());
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new DevicePreGrantByTicketResponse(body.action_ticket)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "参数错误"));
        }
    }
}