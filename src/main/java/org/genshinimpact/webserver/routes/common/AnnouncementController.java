package org.genshinimpact.webserver.routes.common;

// Imports
import org.genshinimpact.webserver.enums.Retcode;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.responses.common.announcement.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"common/hk4e_global/announcement/api", "common/hk4e_cn/announcement/api", "common/announcement/api"}, produces = "application/json")
public final class AnnouncementController {
    /**
     * Source: <a href="https://sg-hk4e-api.hoyoverse.com/common/hk4e_global/announcement/api/getAlertAnn">https://sg-hk4e-api.hoyoverse.com/common/hk4e_global/announcement/api/getAlertAnn</a><br><br>
     *  Description: Fetches information about the announcement in the game.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code bundle_id} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code game} — The client's connected game name. ({@code HK4E} in this case)</li>
     *          <li>{@code game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code lang} — The client's game language name.</li>
     *          <li>{@code level} — The client's level.</li>
     *          <li>{@code platform} — The client's platform name.</li>
     *          <li>{@code region} — The client's connected region name.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *        </ul>
     */
    @GetMapping(value = "getAlertAnn")
    public ResponseEntity<Response<?>> AnnouncementGetAlertAnn() {
        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AnnouncementGetAlertAnnResponse()));
    }

    /**
     * Source: <a href="https://sg-hk4e-api.hoyoverse.com/common/hk4e_global/announcement/api/getAlertPic">https://sg-hk4e-api.hoyoverse.com/common/hk4e_global/announcement/api/getAlertPic</a><br><br>
     *  Description: Fetches information about the announcement in the game.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code bundle_id} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code game} — The client's connected game name. ({@code HK4E} in this case)</li>
     *          <li>{@code game_biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code lang} — The client's game language name.</li>
     *          <li>{@code level} — The client's level.</li>
     *          <li>{@code platform} — The client's platform name.</li>
     *          <li>{@code region} — The client's connected region name.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *        </ul>
     */
    @GetMapping(value = "getAlertPic")
    public ResponseEntity<Response<?>> AnnouncementGetAlertPic() {
        return ResponseEntity.ok(new Response<>(Retcode.RETCODE_SUCC, "OK", new AnnouncementGetAlertPicResponse()));
    }
}