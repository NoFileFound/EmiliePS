package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.embeds.DeviceInfo;
import org.genshinimpact.database.embeds.FatigueRemind;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.PlayerIdentity;
import org.genshinimpact.gameserver.game.player.PlayerPosition;
import org.genshinimpact.gameserver.game.player.PlayerTeam;
import org.genshinimpact.webserver.models.account.device.DeviceInfoModel;

@Getter
@Entity(value = "accounts", useDiscriminator = false)
public final class Account implements PlayerIdentity {
    @Id private final Long id;
    @Setter private String username;
    @Setter private String emailAddress;
    private final String password;
    @Setter private String mobileNumber;
    @Setter private String safeMobileNumber;
    @Setter @Transient private String sessionToken;
    @Setter private String comboToken;
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
    @Setter private Map<String, DeviceInfo> deviceInfo;
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
    @Setter private Long lastLoginDate;
    private final Map<Integer, Avatar> avatars;
    @Setter private Integer currentAvatarId;
    private final Set<Integer> flyCloakList;
    private final Set<Integer> costumeList;
    @Getter private final LinkedHashMap<Integer, PlayerTeam> teamList;
    @Setter private Integer currentTeamId;
    @Setter private PlayerPosition playerPosition;

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
        this.lastLoginDate = System.currentTimeMillis() / 1000;
        this.avatars = new HashMap<>();
        this.currentAvatarId = 0;
        this.flyCloakList = new HashSet<>();
        this.costumeList = new HashSet<>();
        this.teamList = new LinkedHashMap<>();
        this.currentTeamId = 0;
        this.playerPosition = new PlayerPosition();
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
}