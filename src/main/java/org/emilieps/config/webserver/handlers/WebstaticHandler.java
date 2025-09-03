package org.emilieps.config.webserver.handlers;

// Imports
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class WebstaticHandler {
    /**
     *  Source: <a href="https://webstatic.mihoyo.com/">https://webstatic.mihoyo.com/*</a><br><br>
     *  Method: GET<br>
     */
    @GetMapping("webstatic/**")
    public ResponseEntity<?> SendWebstatic(HttpServletRequest request) {
        try {
            String path = request.getRequestURI().replaceFirst("^/webstatic/?", "");
            if (path.isBlank()) {
                return ResponseEntity.status(404).body("");
            }

            Path baseDir = Paths.get("./resources/webstatic").toAbsolutePath().normalize();
            Path targetPath = baseDir.resolve(path).normalize();
            if (!targetPath.startsWith(baseDir)) {
                return ResponseEntity.status(404).body("");
            }

            File file = targetPath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.status(404).body("");
            }

            String mimeType = Files.probeContentType(targetPath);
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeType).body(Files.readAllBytes(targetPath));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}