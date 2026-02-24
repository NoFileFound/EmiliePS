package org.genshinimpact.webserver.routes.mdk;

// Imports
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.MdkTallyListPayPlatformResponse;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/tally/tally","hk4e_cn/mdk/tally/tally","mdk/tally/tally"}, produces = "application/json")
public final class MDKTallyController {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/tally/tally/listPayPlat">https://devapi-takumi.mihoyo.com/mdk/tally/tally/listPayPlat</a><br><br>
     *  Description: Fetches the payment types.<br><br>
     *  Method: GET/POST<br>
     *  Content-Type: application/json<br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code currency} — The provided currency.</li>
     *        </ul>
     */
    @RequestMapping(value = "listPayPlat", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Response<?>> SendTallyListPayPlatforms(@RequestParam(value = "currency", required = false) String currency) {
        if(currency == null || currency.isBlank()) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR2, "Invalid parameter"));
        }

        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new MdkTallyListPayPlatformResponse()));
    }
}