package org.genshinhttpsrv.controllers.abtest;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.database.DBManager;
import org.genshinhttpsrv.database.collections.Experiment;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "data_abtest_api", produces = "application/json")
public final class Abtest implements Response {
    @Override public LinkedHashMap<String, Object> makeResponse(Retcode retcode, String message, Object data) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        response.put("retcode", retcode);
        response.put("success", (data != null));
        response.put("message", message);
        if(data != null) {
            response.put("data", data);
        }

        return response;
    }

    /**
     *  Source: <a href="https://devapi-data-takumi.mihoyo.com/data_abtest_api/config/experiment/list">https://devapi-data-takumi.mihoyo.com/data_abtest_api/config/experiment/list</a><br><br>
     *  Description: Fetches the experiments for the scene.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code app_sign} — The application signature.</li>
     *          <li>{@code uid} — The client's account id.</li>
     *          <li>{@code scene_id} — The client's current scene id.</li>
     *          <li>{@code params} — The given additional parameters.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "config/experiment/list")
    public ResponseEntity<LinkedHashMap<String, Object>> SendExperimentList(@RequestBody ExperimentListModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body == null || body.scene_id == null || body.scene_id.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_PARAMETER_ERROR, Application.getTranslationManager().get(lang, "retcode_parameter_error"), null));
        }

        try {
            String content = String.format("experiment_id=%s&scene_id=%s&uid=%s&key=%s", body.experiment_id, body.scene_id, body.uid, EncryptionManager.getAbTestKeys().get(Integer.parseInt(body.app_id)));
            if(!EncryptionManager.md5Encode(content).equals(body.app_sign)) {
                return ResponseEntity.ok(this.makeResponse(Retcode.RET_ABTEST_AUTHENTICATION_FAILED, Application.getTranslationManager().get(lang, "retcode_abtest_authentication_failed"), null));
            }
        } catch (NumberFormatException ignored) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RET_ABTEST_AUTHENTICATION_FAILED, Application.getTranslationManager().get(lang, "retcode_abtest_authentication_failed"), null));
        }

        List<String> scenes = new ArrayList<>(Arrays.asList(body.scene_id.split(",")));
        List<Experiment> experimentList = new ArrayList<>();
        for(String scene : scenes) {
            List<Experiment> tmp = DBManager.getDataStore().find(Experiment.class).filter(eq("config_id", scene)).stream().toList();
            experimentList.addAll(tmp);
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "", experimentList));
    }


    // Classes
    public static class ExperimentListModel {
        public String app_id;
        public String app_sign;
        public String uid;
        public String scene_id;
        public String experiment_id = "";
        public JsonNode params;
    }
}