package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.models.risky.GeetestModel;

public class RiskyCheckResponse {
    public String id;
    public String action;
    public GeetestModel geetest;

    public RiskyCheckResponse() {
        this.id = "";
        this.action = "ACTION_NONE";
        this.geetest = null;
    }

    public RiskyCheckResponse(String id, GeetestModel geetest) {
        this.id = id;
        this.action = "ACTION_GEETEST";
        this.geetest = geetest;
    }
}