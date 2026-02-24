package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.WebConfig;

public class ComboBoxPorteCNResponse {
    public Vals vals;

    public ComboBoxPorteCNResponse(WebConfig.BoxConfig.PorteCnConfig config) {
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