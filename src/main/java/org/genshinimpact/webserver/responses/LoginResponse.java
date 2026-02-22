package org.genshinimpact.webserver.responses;

// Imports
import static org.genshinimpact.webserver.utils.Utils.filterString;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.utils.GeoIP;

public class LoginResponse {
    public AccountDetails account;
    public Boolean realperson_required;
    public Boolean safe_moblie_required;
    public Boolean reactivate_required;
    public Boolean device_grant_required;
    public String realname_operation;

    public LoginResponse(Account myAccount, String ipAddress) {
        this.account = new AccountDetails(myAccount, ipAddress);
        this.reactivate_required = myAccount.getRequireAccountReactivation();
        this.device_grant_required = myAccount.getRequireDeviceGrant();
        this.safe_moblie_required = myAccount.getRequireSafeMobile();
        this.realperson_required = myAccount.getRequireRealPerson();
        this.realname_operation = myAccount.getRequireRealPersonOperation();
    }

    public static class AccountDetails {
        public String uid;
        public String name;
        public String email;
        public String mobile;
        public Character is_email_verify;
        public String realname;
        public String identity_card;
        public String token;
        public String google_name;
        public String twitter_name;
        public String game_center_name;
        public String apple_name;
        public String sony_name;
        public String tap_name;
        public String country;
        public String reactivate_ticket;
        public String area_code;
        public String device_grant_ticket;
        public String steam_name;
        public String unmasked_email;
        public Integer unmasked_email_type;
        public String cx_name;
        public String safe_mobile;

        public AccountDetails(Account myAccount, String ipAddress) {
            this.uid = String.valueOf(myAccount.getId());
            this.name = myAccount.getUsername();
            this.email = myAccount.getEmailAddress();
            this.mobile = filterString(myAccount.getMobileNumber());
            this.is_email_verify = '1';
            this.realname = filterString(myAccount.getIdentityName());
            this.identity_card = filterString(myAccount.getIdentityCard());
            this.token = myAccount.getSessionToken();
            this.google_name = filterString(myAccount.getGoogleName());
            this.twitter_name = filterString(myAccount.getTwitterName());
            this.game_center_name = filterString(myAccount.getGameCenterName());
            this.apple_name = filterString(myAccount.getAppleName());
            this.sony_name = filterString(myAccount.getSonyName());
            this.tap_name = filterString(myAccount.getTapTapName());
            this.country = GeoIP.getCountryCode(ipAddress);
            this.reactivate_ticket = myAccount.getRequireAccountReactivationTicket();
            this.area_code = "";
            this.device_grant_ticket = myAccount.getRequireDeviceGrantTicket();
            this.steam_name = filterString(myAccount.getSteamName());
            this.unmasked_email = "";
            this.unmasked_email_type = 1;
            this.cx_name = filterString(myAccount.getCxName());
            this.safe_mobile = filterString(myAccount.getSafeMobileNumber());
        }
    }
}