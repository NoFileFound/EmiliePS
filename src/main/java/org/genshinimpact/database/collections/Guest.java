package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.embeds.FatigueRemind;
import org.genshinimpact.gameserver.game.account.AccountBase;

@Getter
@Entity(value = "guests", useDiscriminator = false)
public final class Guest extends AccountBase {
    @Id private final Long id;
    @Setter private String username;
    private final String deviceId;
    @Setter private String comboToken;
    @Transient @Setter private Boolean isNew;
    @Setter private Boolean requireHeartbeat;
    @Setter private FatigueRemind fatigueRemind;

    /**
     * Creates a new guest.
     * @param deviceId The guest's device id.
     */
    public Guest(String deviceId) {
        this.id = DBManager.getCounterValue("lastAccountId");
        this.deviceId = deviceId;
        this.fatigueRemind = null;
        this.requireHeartbeat = false;
    }

    /**
     * Saves the guest instance.
     */
    public void save(boolean updateDb) {
        if(updateDb) {
            DBManager.saveInstance(this);
        }

        DBManager.getCachedGuests().put(this.id, this);
    }

    /**
     * Checks if the player is Guest.
     * @return True.
     */
    @Override
    public boolean isGuest() {
        return true;
    }
}