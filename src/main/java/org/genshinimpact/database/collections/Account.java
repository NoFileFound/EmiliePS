package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.embeds.DeviceInfo;

@Getter
@Entity(value = "accounts", useDiscriminator = false)
public final class Account {
    @Id private final Long id;
    private String username;
    @Setter private String emailAddress;
    private final String password;
    @Setter private String mobileNumber;
    @Setter private String safeMobileNumber;
    @Setter private String sessionToken;
    @Setter private String identityName;
    @Setter private String identityCard;
    private String googleName;
    private String twitterName;
    private String appleName;
    private String cxName;
    private String gameCenterName;
    private String sonyName;
    private String tapTapName;
    private String steamName;
    private final Map<String, DeviceInfo> deviceInfo;
    private Boolean requireAccountReactivation;
    private String requireAccountReactivationTicket;
    private Boolean requireDeviceGrant;
    private String requireDeviceGrantTicket;
    private Boolean requireSafeMobile;
    private String requireSafeMobileTicket;
    private Boolean requireRealPerson;
    private String requireRealPersonTicket;
    private String requireRealPersonOperation;

    /**
     * Creates a new account.
     */
    public Account(String emailAddress, String password, String ipAddress, String deviceInfo) {
        this.id = DBManager.getCounterValue("lastAccountId");
        this.emailAddress = emailAddress;
        this.password = password;
        this.deviceInfo = new HashMap<>();
        this.deviceInfo.put(deviceInfo, new DeviceInfo(ipAddress));
    }

    /**
     * Saves the account instance.
     */
    public void save() {
        DBManager.saveInstance(this);
        DBManager.getCachedAccounts().put(this.id, this);
    }

    /**
     * Sets whether the account requires device verification.
     *
     * @param enable True to require account device verification, false to remove the requirement.
     * @param ticketId The ID of the device verification ticket.
     */
    public void setDeviceGrant(boolean enable, String ticketId) {
        this.requireDeviceGrant = enable;
        this.requireDeviceGrantTicket = (enable ? ticketId : null);
    }

    /**
     * Sets whether the account requires reactivation.
     *
     * @param enable True to require account reactivation, false to remove the requirement.
     * @param ticketId The ID of the reactivation ticket.
     */
    public void setRequireActivation(boolean enable, String ticketId) {
        this.requireAccountReactivation = enable;
        this.requireAccountReactivationTicket = (enable ? ticketId : null);
    }
}