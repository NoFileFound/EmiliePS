package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.embeds.DeviceInfo;
import org.genshinimpact.database.embeds.FatigueRemind;
import org.genshinimpact.gameserver.game.account.AccountBase;
import org.genshinimpact.webserver.models.account.device.DeviceInfoModel;

@Getter
@Entity(value = "accounts", useDiscriminator = false)
public final class Account extends AccountBase {
    @Id private final Long id;
    @Setter private String username;
    @Setter private String emailAddress;
    @Setter private String password;
    @Setter private String mobileNumber;
    @Setter private String safeMobileNumber;
    @Transient @Setter private String sessionToken;
    @Setter private String comboToken;
    @Setter private String identityName;
    @Setter private String identityCard;
    @Setter private Map<String, DeviceInfo> deviceInfo;
    private String googleName;
    private String twitterName;
    private String appleName;
    private String cxName;
    private String gameCenterName;
    private String sonyName;
    private String tapTapName;
    private String steamName;
    private Boolean requireAccountReactivation;
    private String requireAccountReactivationTicket;
    private Boolean requireDeviceGrant;
    private String requireDeviceGrantTicket;
    @Setter private Boolean requireSafeMobile;
    @Setter private Boolean requireRealPerson;
    @Setter private String requireRealPersonOperation;
    @Setter private String emailBindTicket;
    @Setter private Boolean requireHeartbeat;
    @Setter private Boolean isPendingDeletion;
    @Setter private FatigueRemind fatigueRemind;
    private final Map<Integer, Boolean> agreementInfos;

    public Account(String emailAddress, String password, DeviceInfoModel deviceInfo) {
        this.id = DBManager.getCounterValue("lastAccountId");
        this.emailAddress = emailAddress;
        this.password = password;
        this.deviceInfo = new HashMap<>();
        this.deviceInfo.put(deviceInfo.device_id, new DeviceInfo(deviceInfo));
        this.fatigueRemind = null;
        this.requireHeartbeat = false;
        this.isPendingDeletion = false;
        this.agreementInfos = new HashMap<>();
        this.requireAccountReactivation = false;
        this.requireDeviceGrant = false;
        this.requireRealPerson = false;
        this.requireSafeMobile = false;
    }

    /**
     * Saves the account instance.
     */
    public void save(boolean updateDb) {
        if(updateDb) {
            DBManager.saveInstance(this);
        }

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

    /**
     * Checks if the player is Guest.
     * @return False.
     */
    @Override
    public boolean isGuest() {
        return false;
    }
}