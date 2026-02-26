package org.genshinimpact.webserver.responses.combo.granter;

// Imports
import org.genshinimpact.database.embeds.FatigueRemind;
import org.genshinimpact.webserver.enums.AccountType;

public class GranterLoginResponse {
    public String combo_id;
    public String open_id;
    public String combo_token;
    public Boolean heartbeat;
    public Integer account_type;
    public FatigueRemind fatigue_remind;
    public GranterLoginData data;

    public GranterLoginResponse(String open_id, String combo_token, Boolean heartbeat, AccountType accountType, String country_code, Boolean is_new_register, FatigueRemind fatigue_remind) {
        this.combo_id = "0";
        this.open_id = open_id;
        this.combo_token = combo_token;
        this.heartbeat = heartbeat;
        this.fatigue_remind = fatigue_remind;
        this.account_type = accountType.getValue();
        this.data = new GranterLoginData(country_code, (accountType == AccountType.ACCOUNT_GUEST), is_new_register);
    }

    public static class GranterLoginData {
        public String country;
        public Boolean guest;
        public Boolean is_new_register;

        public GranterLoginData(String country_code, Boolean is_guest, Boolean is_new_register) {
            this.country = country_code;
            this.guest = is_guest;
            this.is_new_register = is_new_register;
        }
    }
}