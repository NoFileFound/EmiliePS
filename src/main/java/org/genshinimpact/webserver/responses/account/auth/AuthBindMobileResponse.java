package org.genshinimpact.webserver.responses.account.auth;

public class AuthBindMobileResponse {
    public String bindmobile_operation;
    public Long uid;
    public String name;
    public String email;
    public String mobile;
    public String mobile_area;

    public AuthBindMobileResponse(Long uid, String name, String email, String mobile, String mobile_area) {
        this.bindmobile_operation = "completed";
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.mobile_area = mobile_area;
    }
}