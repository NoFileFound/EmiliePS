package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import java.util.HashMap;
import java.util.HashSet;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.gameserver.game.account.AccountBase;
import org.genshinimpact.gameserver.game.player.PlayerPosition;
import org.genshinimpact.gameserver.game.team.Team;

@Getter
@Entity(value = "guests", useDiscriminator = false)
public final class Guest extends AccountBase {
    @Id private final Long id;
    @Setter private String username;
    private final String deviceId;
    @Setter private String comboToken;
    @Transient @Setter private Boolean isNew;

    /**
     * Creates a new guest.
     * @param deviceId The guest's device id.
     */
    public Guest(String deviceId) {
        this.id = DBManager.getCounterValue("lastAccountId");
        this.username = "Guest";
        this.deviceId = deviceId;
        this.lastLoginDate = System.currentTimeMillis() / 1000;
        this.unlockedAvatars = new HashMap<>();
        this.mainCharacterId = 0;
        this.profileAvatarImageId = 0;
        this.playerPosition = new PlayerPosition();
        this.playerRotation = new PlayerPosition(0, 0, 0);
        this.playerTeam = new Team();
        this.ownedFlyCloakList = new HashSet<>();
        this.ownedCostumeList = new HashSet<>();
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