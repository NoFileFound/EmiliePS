package org.genshinimpact.webserver.responses.account.auth;

public class AuthBindRealNameResponse {
    public String realname_operation;
    public Long uid;
    public String name;
    public String email;
    public String identity_card;
    public String realname;

    public AuthBindRealNameResponse(Long uid, String name, String email, String identity_card, String realname) {
        this.realname_operation = "completed";
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.identity_card = identity_card;
        this.realname = realname;
    }
}