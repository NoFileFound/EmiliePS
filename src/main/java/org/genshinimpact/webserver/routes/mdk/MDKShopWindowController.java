package org.genshinimpact.webserver.routes.mdk;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.mdk.shopwindow.*;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/shopwindow/shopwindow","hk4e_cn/mdk/shopwindow/shopwindow","mdk/shopwindow/shopwindow"}, produces = "application/json")
public final class MDKShopWindowController {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shopwindow/shopwindow/listPriceTier">https://devapi-takumi.mihoyo.com/mdk/shopwindow/shopwindow/listPriceTier</a><br><br>
     *  Description: Fetches the payment tiers.<br><br>
     *  Method: GET/POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code currency} — The given currency.</li>
     *        </ul>
     */
    @RequestMapping(value = {"listPriceTier", "listPriceTierV2"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Response<?>> SendShopWindowListPriceTiers(HttpServletRequest request, @RequestParam(value = "game_biz", required = false) String game_biz, @RequestParam(value = "currency", required = false) String currency) {
        try {
            AppName appName = AppName.fromValue(game_biz);
            if(appName == null || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "game biz missing"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShopWindowListPriceTierResponse((currency != null ? currency : GeoIP.getCountryCurrency(request.getRemoteAddr())))));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "game biz missing"));
        }
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/shopwindow/shopwindow/getCurrencyAndCountryByIp">https://devapi-takumi.mihoyo.com/mdk/shopwindow/shopwindow/getCurrencyAndCountryByIp</a><br><br>
     *  Description: Fetches the country code and currency from the ip address.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *        </ul>
     */
    @GetMapping(value = "getCurrencyAndCountryByIp")
    public ResponseEntity<Response<?>> SendShopWindowGetCurrencyAndCountryByIp(HttpServletRequest request, String game_biz) {
        try {
            AppName appName = AppName.fromValue(game_biz);
            if(appName == null || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "game biz missing"));
            }

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new ShopWindowGetCurrencyAndCountryByIpResponse(GeoIP.getCountryCode(request.getRemoteAddr()), GeoIP.getCountryCurrency(request.getRemoteAddr()))));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "game biz missing"));
        }
    }
}