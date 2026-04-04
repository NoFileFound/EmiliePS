package org.genshinimpact.gameserver.game.account;

// Imports
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.PlayerPosition;
import org.genshinimpact.gameserver.game.team.Team;

/**
 * The required variables for work in the game server.
 */
public abstract class AccountBase implements PlayerIdentity {
    /**
     * The player's last login time in the game.
     */
    @Getter @Setter protected Long lastLoginDate;

    /**
     * The player's unlocked avatars.
     */
    @Getter protected Map<Integer, Avatar> unlockedAvatars;

    /**
     * The player's current avatar id.
     */
    @Getter @Setter protected int mainCharacterId;

    /**
     * The player's profile avatar head image id.
     */
    @Getter @Setter protected int profileAvatarImageId;

    /**
     * The player's owned costume list.
     */
    @Getter protected Set<Integer> ownedCostumeList;

    /**
     * The player's owned fly cloak list.
     */
    @Getter protected Set<Integer> ownedFlyCloakList;

    /**
     * The player's team.
     */
    @Getter protected Team playerTeam;

    /**
     * The player's position in the game.
     */
    @Getter @Setter protected PlayerPosition playerPosition;

    /**
     * The player's rotation in the game.
     */
    @Getter @Setter protected PlayerPosition playerRotation;

    /**
     * Checks if the player is guest or not.
     * @return True if its guest or else False.
     */
    public abstract boolean isGuest();
}