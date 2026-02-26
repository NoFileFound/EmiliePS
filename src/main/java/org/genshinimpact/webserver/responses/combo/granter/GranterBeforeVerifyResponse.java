package org.genshinimpact.webserver.responses.combo.granter;

public class GranterBeforeVerifyResponse {
    public Boolean is_heartbeat_required;
    public Boolean is_realname_required;
    public Boolean is_guardian_required;

    public GranterBeforeVerifyResponse(Boolean is_heartbeat_required, Boolean is_realname_required) {
        this.is_heartbeat_required = is_heartbeat_required;
        this.is_realname_required = is_realname_required;
        this.is_guardian_required = false;
    }
}