package org.genshinimpact.webserver.responses.combo.guard;

public class GuardPing2Response {
    public Boolean banned;
    public String msg;
    public Long interval;

    public GuardPing2Response() {
        this.banned = false;
        this.msg = "";
        this.interval = 0L;
    }

    public GuardPing2Response(Boolean banned, String msg, Long interval) {
        this.banned = banned;
        this.msg = msg;
        this.interval = interval;
    }
}