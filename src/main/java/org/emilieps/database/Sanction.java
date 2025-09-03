package org.emilieps.database;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.Setter;
import org.emilieps.data.enums.BanReasonType;

// Libraries
import org.emilieps.library.MongodbLib;

@Entity(value = "sanctionlogs", useDiscriminator = false)
@Getter
public final class Sanction {
    private final @Id long id;
    private final Long accountId;
    private final String ipAddress;
    private final String author;
    private final BanReasonType reason;
    private final Boolean isPermanent;
    @Setter private String state;
    private final Long createdDate;
    private final Long expirationDate;

    /**
     * Creates a new sanction.
     * @param accountId The account id.
     * @param ipAddress The account's ip address.
     * @param author The sanction author (moderator).
     * @param reason The sanction reason.
     * @param endDate The sanction expiration date.
     */
    public Sanction(final Long accountId, final String ipAddress, final String author, final BanReasonType reason, final Long endDate) {
        this.id = MongodbLib.getCounterValue("lastSanctionId");
        this.accountId = accountId;
        this.ipAddress = ipAddress;
        this.author = author;
        this.reason = reason;
        this.createdDate = System.currentTimeMillis();
        if(endDate == -1L) {
            this.isPermanent = true;
            this.expirationDate = -1L;
        } else {
            this.isPermanent = false;
            this.expirationDate = endDate;
        }
        this.state = "Active";
    }

    /**
     * Saves the sanction.
     */
    public void save() {
        MongodbLib.saveInstance(this);
    }
}