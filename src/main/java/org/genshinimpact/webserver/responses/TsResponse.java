package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.enums.Retcode;

public class TsResponse {
    public Retcode code;
    public String message;
    public String milliTs;

    public TsResponse(Long milliTs) {
        this.code = Retcode.RETCODE_SUCC;
        this.message = "app running";
        this.milliTs = String.valueOf(milliTs);
    }

    public TsResponse() {
        this.code = Retcode.RETCODE_FAIL;
        this.message = "fail";
        this.milliTs = "0";
    }
}