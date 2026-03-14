package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.PlayerIdentity;

@Getter
@Entity(value = "guests", useDiscriminator = false)
public final class Guest implements PlayerIdentity {
    @Id private final Long id;
    @Setter private String username;
    private final String deviceId;
    @Setter private String comboToken;
    @Setter @Transient Boolean isNew;
    @Setter private Boolean requireHeartbeat;
    private final Map<Integer, Avatar> avatars;
    @Setter private Integer currentAvatarId;

    /**
     * Creates a new guest.
     * @param deviceId The guest's device id.
     */
    public Guest(String deviceId) {
        this.id = DBManager.getCounterValue("lastGuestId");
        this.username = "Guest";
        this.deviceId = deviceId;
        this.requireHeartbeat = false;
        this.avatars = new HashMap<>();
        this.currentAvatarId = 0;
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
}