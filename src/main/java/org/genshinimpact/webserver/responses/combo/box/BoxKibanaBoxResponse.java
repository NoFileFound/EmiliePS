package org.genshinimpact.webserver.responses.combo.box;

// Imports
import org.genshinimpact.configs.WebConfig;

public class BoxKibanaBoxResponse {
    public WebConfig.BoxConfig.PorteOsKibanaConfig vals;

    public BoxKibanaBoxResponse(WebConfig.BoxConfig.PorteOsKibanaConfig vals) {
        this.vals = vals;
    }
}