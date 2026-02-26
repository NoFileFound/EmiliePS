package org.genshinimpact.webserver.responses.mdk.guest;

public class GuestLoginResponse {
    public Long guest_id;
    public Boolean newly;

    public GuestLoginResponse(Long guest_id, Boolean newly) {
        this.guest_id = guest_id;
        this.newly = newly;
    }
}