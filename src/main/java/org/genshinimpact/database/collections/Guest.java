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
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.PlayerIdentity;
import org.genshinimpact.gameserver.game.player.PlayerPosition;
import org.genshinimpact.gameserver.game.player.PlayerTeam;

@Getter
@Entity(value = "guests", useDiscriminator = false)
public final class Guest implements PlayerIdentity {
    @Id private final Long id;
    @Setter private String username;
    private final String deviceId;
    @Setter private String comboToken;
    @Setter @Transient Boolean isNew;
    @Setter private Boolean requireHeartbeat;
    @Setter private Long lastLoginDate;
    private final Map<Integer, Avatar> avatars;
    @Setter private Integer currentAvatarId;
    private final Set<Integer> flyCloakList;
    private final Set<Integer> costumeList;
    @Getter private final LinkedHashMap<Integer, PlayerTeam> teamList;
    @Setter private Integer currentTeamId;
    @Setter private PlayerPosition playerPosition;

    /**
     * Creates a new guest.
     * @param deviceId The guest's device id.
     */
    public Guest(String deviceId) {
        this.id = DBManager.getCounterValue("lastAccountId");
        this.username = "Guest";
        this.deviceId = deviceId;
        this.requireHeartbeat = false;
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
     * Saves the guest instance.
     */
    public void save(boolean updateDb) {
        if(updateDb) {
            DBManager.saveInstance(this);
        }

        DBManager.getCachedGuests().put(this.id, this);
    }
}