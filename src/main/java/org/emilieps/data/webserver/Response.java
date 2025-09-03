package org.emilieps.data.webserver;

// Imports
import java.util.LinkedHashMap;
import org.emilieps.data.HttpRetcode;

public interface Response {
    /**
     * Creates a json response.
     * @param retcode The return code.
     * @param message The message.
     * @param data Additional data if is required.
     * @return Json response as Hashmap.
     */
    default LinkedHashMap<String, Object> makeResponse(HttpRetcode retcode, String message, Object data) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        response.put("retcode", retcode);
        response.put("message", message);
        response.put("data", data);

        return response;
    }
}