package org.emilieps.bootspring.configuration;

// Imports
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public final class ExHandler {
    /**
     * Handles the bad request exception. (http status code 400)
     * @param ex Contains information about the page.
     * @return Json body.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handle400(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(new LinkedHashMap<>() {{
            put("data", null);
            put("retcode", (ex.getCause() instanceof JsonProcessingException || ex.getCause() instanceof JsonParseException) ? -502 : 400);
            put("message", "Something went wrong...please retry later");
        }}, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles the not found exception. (http status code 404)
     * @return Json body.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handle404() {
        return new ResponseEntity<>(new LinkedHashMap<>() {{
            put("data", null);
            put("retcode", 404);
            put("message", "The requested resource was not found.");
        }}, HttpStatus.NOT_FOUND);
    }
}