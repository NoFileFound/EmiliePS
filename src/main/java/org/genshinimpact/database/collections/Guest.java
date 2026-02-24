package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;

@Getter
@Entity(value = "guests", useDiscriminator = false)
public final class Guest {
    @Id private final Long id;
    private final String deviceId;
    @Setter private String comboToken;
    @Setter @Transient Boolean isNew;
    @Setter private Boolean requireHeartbeat;

    /**
     * Creates a new guest.
     * @param deviceId The guest's device id.
     */
    public Guest(String deviceId) {
        this.id = DBManager.getCounterValue("lastGuestId");
        this.deviceId = deviceId;
        this.requireHeartbeat = false;
    }

    /**
     * Saves the guest instance.
     */
    public void save() {
        DBManager.saveInstance(this);
    }
}