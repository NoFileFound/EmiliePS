package org.genshinimpact.webserver.responses;

public class DeviceGrantResponse {
    public String login_ticket;
    public String game_token;

    public DeviceGrantResponse(String login_ticket, String game_token) {
        this.login_ticket = login_ticket;
        this.game_token = game_token;
    }
}