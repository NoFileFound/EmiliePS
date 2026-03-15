package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;

@Entity(value = "sanctions", useDiscriminator = false)
@Getter
public final class Sanction {
    @Id private final Long id;
    private final Long accountId;
    private final String author;
    private final Long createdDate;
    private final Long expirationDate;
    private final SanctionType sanctionType;
    @Setter private String state;

    /**
     * Creates a new sanction.
     * @param accountId The account id.
     * @param author The sanction author.
     * @param endDate When will the sanction expire.
     */
    public Sanction(Long accountId, String author, Long endDate) {
        this.id = DBManager.getCounterValue("lastSanctionId");
        this.accountId = accountId;
        this.author = author;
        this.createdDate = System.currentTimeMillis() / 1000;
        this.expirationDate = endDate;
        this.sanctionType = SanctionType.FORBID_CHEATING_PLUGINS;
        this.state = "Active";
    }

    /**
     * Deletes a sanction.
     */
    public void delete() {
        DBManager.deleteInstance(this);
    }

    /**
     * Saves a sanction.
     */
    public void save() {
        DBManager.saveInstance(this);
    }


    enum SanctionType {
        FORBID_CHEATING_PLUGINS,
        FORBID_CHAT_INVALID,
        FORBID_PLAYER_DATA_ERROR,
        FORBID_ABNORMAL_BEHAVIOR,
        FORBID_BEHAVIOR_INVALID
    }
}