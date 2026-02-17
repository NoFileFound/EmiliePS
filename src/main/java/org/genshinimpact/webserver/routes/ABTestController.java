package org.genshinimpact.webserver.routes;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_ABTEST_AUTHENTICATION_FAILED;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_PARAMETER_ERROR;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import org.genshinimpact.database.Database;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.models.ABTestExperimentsListModel;
import org.genshinimpact.webserver.responses.ABTestExperimentsListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "data_abtest_api", produces = "application/json")
public final class ABTestController {
    private static final Map<Integer, String> abTestKeys = Map.ofEntries(
            Map.entry(60, "d10ff485-06ec-4b9d-8977-14716c0a1dda"),
            Map.entry(31, "5f876baa-c4c4-43df-880a-c026184fd01c"),
            Map.entry(28, "df7f6400-ae6e-4850-8ff1-63e1f3f960d6"),
            Map.entry(45, "2902c529-499a-4a9b-a7b3-3b675632b8c3"),
            Map.entry(47, "b437f6e3-7e48-445a-8d8b-c8ebb5bd2b3e"));

    /**
     * Source: <a href="https://devapi-data-takumi.mihoyo.com/data_abtest_api/config/experiment/list">https://devapi-data-takumi.mihoyo.com/data_abtest_api/config/experiment/list</a><br><br>
     *  Description: Fetches the experiments for the current scene.<br><br>
     *  Methods: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code app_sign} — The application signature.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *          <li>{@code scene_id} — The client's current scene id.</li>
     *          <li>{@code params} — The given additional parameters.</li>
     *        </ul>
     */
    @PostMapping(value = "config/experiment/list")
    public ResponseEntity<ABTestExperimentsListResponse<?>> SendExperimentList(HttpServletRequest request) {
        ABTestExperimentsListModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), ABTestExperimentsListModel.class);
        } catch(Exception ignored) {
            return ResponseEntity.ok(new ABTestExperimentsListResponse<>(RETCODE_PARAMETER_ERROR, "参数错误"));
        }

        String content = String.format("experiment_id=%s&scene_id=%s&uid=%s&key=%s", body.experiment_id, body.scene_id, body.uid, abTestKeys.get(Integer.parseInt(body.app_id)));
        if(!CryptoUtils.getMd5(content.getBytes()).equals(body.app_sign)) {
            return ResponseEntity.ok(new ABTestExperimentsListResponse<>(RETCODE_ABTEST_AUTHENTICATION_FAILED, "认证失败"));
        }

        return ResponseEntity.ok(new ABTestExperimentsListResponse<>(RETCODE_SUCC, "", Database.getSceneExperiments(Arrays.asList(body.scene_id.split(",")))));
    }
}