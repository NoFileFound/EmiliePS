package org.genshinhttpsrv.controllers;

// Imports
import org.genshinhttpsrv.api.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "sdk_global/marker/api", produces = "application/json")
public final class Marker implements Response {


    // Classes
    public static class GenMarkModel {
        public String app_id;
        public String content;
        public int height;
        public int width;
    }
}