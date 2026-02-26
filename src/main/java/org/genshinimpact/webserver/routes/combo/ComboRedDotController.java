package org.genshinimpact.webserver.routes.combo;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.models.combo.redddot.*;
import org.genshinimpact.webserver.responses.combo.reddot.*;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"combo/red_dot", "takumi/combo/red_dot"}, produces = "application/json")
public final class ComboRedDotController {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/red_dot/list">https://devapi-takumi.mihoyo.com/combo/red_dot/list</a><br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code uid} — The client's account id.</li>
     *          <li>{@code region} — The client's server region name.</li>
     *          <li>{@code game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code player_level} — The client's player level.</li>
     *        </ul>
     */
    @PostMapping("list")
    public ResponseEntity<Response<?>> SendComboRedDotList(HttpServletRequest request) {
        RedDotListModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), RedDotListModel.class);
            AppName appName = AppName.fromValue(body.game_biz);
            if(appName == null || appName == AppName.APP_UNKNOWN || body.region == null || body.region.isBlank() || body.uid == null || body.uid.isBlank() || body.player_level == null) {
                return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "params error"));
            }

            ///  TODO: Dynamic add red dots

            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new RedDotListResponse()));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(Retcode.RETCODE_PARAMETER_ERROR, "params error"));
        }
    }
}