package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.enums.AccountType;

public class GranterLoginResponse {
    public String combo_id;
    public String open_id;
    public String combo_token;
    public Boolean heartbeat;
    public Integer account_type;
    public GranterLoginData data;

    public GranterLoginResponse(String open_id, String combo_token, Boolean heartbeat, AccountType accountType) {
        this.combo_id = "0";
        this.open_id = open_id;
        this.combo_token = combo_token;
        this.heartbeat = heartbeat;
        this.account_type = accountType.getValue();
        this.data = new GranterLoginData();
    }

    public static class GranterLoginData {
        public String country;
        public Boolean guest;
        public Boolean is_new_register;

        public GranterLoginData() {
            this.country = "JP";
            this.guest = false;
            this.is_new_register = false;
        }
    }
}

///  TODO: FINISH