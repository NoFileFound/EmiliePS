package org.genshinimpact.webserver.routes.mdk;

//  Imports
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.mdk.luckycat.*;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/luckycat/luckycat","hk4e_cn/mdk/luckycat/luckycat","mdk/luckycat/luckycat"}, produces = "application/json")
public final class MDKLuckycatController {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/luckycat/luckycat/listPayPlat">https://devapi-takumi.mihoyo.com/mdk/luckycat/luckycat/listPayPlat</a><br><br>
     *  Description: Fetches the payment platforms.<br><br>
     *  Method: GET/POST<br>
     *  Content-Type: application/json<br>
     */
    @RequestMapping(value = "listPayPlat", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Response<?>> SendLuckycatListPayPlatforms() {
        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new LuckycatListPayPlatformResponse()));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/luckycat/luckycat/listPriceTier">https://devapi-takumi.mihoyo.com/mdk/luckycat/luckycat/listPriceTier</a><br><br>
     *  Description: Fetches the payment tiers.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code currency} — The given currency.</li>
     *        </ul>
     */
    @PostMapping(value = "listPriceTier")
    public ResponseEntity<Response<?>> SendLuckycatListPriceTiers(HttpServletRequest request, @RequestParam(value = "currency", required = false) String currency) {
        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new LuckycatListPriceTierResponse((currency != null ? currency : GeoIP.getCountryCurrency(request.getRemoteAddr())))));
    }
}