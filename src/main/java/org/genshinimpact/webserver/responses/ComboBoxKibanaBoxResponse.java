package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.WebConfig;

public class ComboBoxKibanaBoxResponse {
    public WebConfig.BoxConfig.PorteOsKibanaConfig vals;

    public ComboBoxKibanaBoxResponse(WebConfig.BoxConfig.PorteOsKibanaConfig vals) {
        this.vals = vals;
    }
}