package org.genshinimpact.webserver.routes.telemetry;

// Imports
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_FAIL;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import jakarta.servlet.http.HttpServletRequest;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.webserver.models.telemetry.CollectVariantsModel;
import org.genshinimpact.webserver.responses.Response;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ShaderCollectController {
    /**
     * Source: <a href="https://apm-log-upload.mihoyo.com/collect_variants">https://apm-log-upload.mihoyo.com/collect_variants</a><br><br>
     * Description: Collects the shader variants.<br><br>
     * Method: POST<br>
     * Content-Type: application/json<br>
     */
    @PostMapping(value = "collect_variants")
    public ResponseEntity<Response<?>> SendCollectVariants(HttpServletRequest request) {
        CollectVariantsModel body;
        try {
            body = JsonUtils.read(request.getInputStream(), CollectVariantsModel.class);
            if(body.productname == null || body.productname.isBlank() || body.version == null || body.shaders == null) {
                return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
            }
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(RETCODE_FAIL, "请求格式错误"));
        }

        ///  TODO: Find out what mihoyo is doing with this one.
        AppBootstrap.getLogger().info("[H5Log] Shader variants collection completed.");
        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK"));
    }
}