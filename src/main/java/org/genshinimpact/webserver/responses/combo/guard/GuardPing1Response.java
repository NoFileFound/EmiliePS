package org.genshinimpact.webserver.responses.combo.guard;

public class GuardPing1Response {
    public Boolean stop;
    public String msg;
    public Long interval;

    public GuardPing1Response() {
        this.stop = false;
        this.msg = "";
        this.interval = 0L;
    }

    public GuardPing1Response(Boolean stop, String msg, Long interval) {
        this.stop = stop;
        this.msg = msg;
        this.interval = interval;
    }
}