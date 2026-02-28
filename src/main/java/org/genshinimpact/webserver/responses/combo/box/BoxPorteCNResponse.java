package org.genshinimpact.webserver.responses.combo.box;

// Imports
import org.genshinimpact.configs.WebConfig;

public class BoxPorteCNResponse {
    public Vals vals;

    public BoxPorteCNResponse(WebConfig.BoxConfig.PorteCnConfig config) {
        this.vals = new Vals(config);
    }

    // Inner class for "vals"
    public static class Vals {
        public WebConfig.BoxConfig.PorteCnConfig cryptoConfig;

        public Vals(WebConfig.BoxConfig.PorteCnConfig cryptoConfig) {
            this.cryptoConfig = cryptoConfig;
        }
    }
}