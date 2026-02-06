package org.genshinimpact.webserver.responses;

public class GetFpResponse {
    public Integer code;
    public String msg;
    public String device_fp;

    public GetFpResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.device_fp = "";
    }

    public GetFpResponse(Integer code, String msg, String device_fp) {
        this.code = code;
        this.msg = msg;
        this.device_fp = device_fp;
    }
}