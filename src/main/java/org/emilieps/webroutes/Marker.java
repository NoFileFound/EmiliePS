package org.emilieps.webroutes;

// Imports
import org.emilieps.data.webserver.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "sdk_global/marker/api", produces = "application/json")
public final class Marker implements Response {
    /// TODO: Implement

    // Classes
    public static class GenMarkModel {
        public String app_id;
        public String content;
        public int height;
        public int width;
    }
}