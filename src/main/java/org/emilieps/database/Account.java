package org.emilieps.database;

// Imports
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.emilieps.data.enums.webserver.RealNameOperation;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.MongodbLib;

@Getter
@Entity(value = "accounts", useDiscriminator = false)
public final class Account {
    @Id private final Long _id;
    @Setter private String name;
    @Setter private Long playerId;
    @Setter private String emailAddress;
    @Setter private Boolean isEmailVerified;
    @Setter private String password;
    @Setter private String ipAddress;
    @Setter private String gameToken;
    @Setter private String countryCode;
    @Setter private String mobileNumber;
    @Setter private String mobileNumberArea;
    @Setter private String safeMobileNumber;
    @Setter private String realname;
    @Setter private String identityCard;
    @Setter private String facebookName;
    @Setter private String googleName;
    @Setter private String twitterName;
    @Setter private String gameCenterName;
    @Setter private String appleName;
    @Setter private String sonyName;
    @Setter private String tapName;
    @Setter private String steamName;
    @Setter private String cxName;
    @Setter private FatigueRemindData fatigueRemind;
    private final Long registeredDate;
    private final Boolean isGuest;
    @Setter private Boolean isRequireGuardian;
    @Setter private Boolean isRequireHeartbeat;
    @Setter private Boolean isRequireRealname;
    @Setter private RealNameOperation realNameOperation;
    @Setter private Boolean isRequireSafeMobile;
    @Setter private Boolean isRequireReactivation;
    @Setter private Boolean isRequireDeviceGrant;
    private final ArrayList<String> acceptedAgreements;
    private final ArrayList<String> deniedAgreements;
    private final List<String> approvedDevices;
    @Setter private String desktopName;
    @Setter private Long lastDisconnectionDate;
    @Setter private String lastGameBiz;
    private final Integer regPlatform;

    /**
     * Creates a new account.
     */
    public Account(String emailAddress, String password, String ipAddress, String country_code, Boolean is_guest, String device_id, Integer regPlatform) {
        this._id = MongodbLib.getCounterValue("lastAccountId");
        this.playerId = -1L;
        this.name = "";
        this.emailAddress = emailAddress;
        this.isEmailVerified = false;
        this.password = password;
        this.ipAddress = ipAddress;
        this.countryCode = country_code;
        this.realname = "";
        this.mobileNumber = "";
        this.mobileNumberArea = "**";
        this.safeMobileNumber = "";
        this.identityCard = "";
        this.facebookName = "";
        this.googleName = "";
        this.twitterName = "";
        this.gameCenterName = "";
        this.appleName = "";
        this.sonyName = "";
        this.tapName = "";
        this.steamName = "";
        this.cxName = "";
        this.fatigueRemind = null;
        this.registeredDate = System.currentTimeMillis();
        this.isGuest = is_guest;
        this.isRequireGuardian = false;
        this.isRequireHeartbeat = false;
        this.isRequireRealname = false;
        this.realNameOperation = RealNameOperation.None;
        this.isRequireSafeMobile = false;
        this.isRequireReactivation = false;
        this.isRequireDeviceGrant = false;
        this.acceptedAgreements = new ArrayList<>();
        this.deniedAgreements = new ArrayList<>();
        this.approvedDevices = new ArrayList<>(List.of(device_id));
        this.lastDisconnectionDate = 0L;
        this.regPlatform = regPlatform;

        this.generateGameToken();
    }

    /**
     * Generates a new session key for the guest account.
     * @return The new session key.
     */
    public String generateGameToken() {
        this.gameToken = EncryptionLib.generateRandomKey(20);
        this.save();
        return this.gameToken;
    }

    /**
     * Updates the database of player.
     */
    public void save() {
        MongodbLib.saveInstance(this);
    }


    // Classes
    @Embedded
    public static class FatigueRemindData {
        private List<Integer> durations;
        private String nickname;
        private int resetPoint;
    }
}