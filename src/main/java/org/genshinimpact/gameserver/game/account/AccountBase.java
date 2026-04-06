package org.genshinimpact.gameserver.game.account;

// Imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.PlayerBirthday;
import org.genshinimpact.gameserver.game.player.PlayerPosition;
import org.genshinimpact.gameserver.game.team.Team;

/**
 * The required variables for work in the game server.
 */
public abstract class AccountBase implements PlayerIdentity {
    /**
     * The player's last login time in the game.
     */
    @Getter @Setter protected Long lastLoginDate = System.currentTimeMillis() / 1000;

    /**
     * The player's unlocked avatars.
     */
    @Getter protected Map<Integer, Avatar> unlockedAvatars = new HashMap<>();

    /**
     * The player's unlocked avatars that are shown in the profile.
     */
    @Getter @Setter protected Set<Integer> unlockedAvatarsProfileShown = new HashSet<>();

    /**
     * The player's unlocked name cards.
     */
    @Getter protected Set<Integer> unlockedNameCards = new HashSet<>(Set.of(210001));

    /**
     * The player's unlocked name cards that are shown in the profile.
     */
    @Getter @Setter protected Set<Integer> unlockedNameCardsProfileShown = new HashSet<>();

    /**
     * The player's current avatar id.
     */
    @Getter @Setter protected int mainCharacterId = 0;

    /**
     * The player's current name card id.
     */
    @Getter @Setter protected int nameCardId = 210001;

    /**
     * The player's signature.
     */
    @Getter @Setter protected String profileSignature = "";

    /**
     * The player's profile avatar head image id.
     */
    @Getter @Setter protected int profileAvatarImageId = 0;

    /**
     * The player's profile avatar's costume head image id.
     */
    @Getter @Setter protected int profileAvatarCostumeImageId = 0;

    /**
     * The player's owned costume list.
     */
    @Getter protected Set<Integer> ownedCostumeList = new HashSet<>();

    /**
     * The player's owned fly cloak list.
     */
    @Getter protected Set<Integer> ownedFlyCloakList = new HashSet<>();

    /**
     * The player's birthday.
     */
    @Getter protected PlayerBirthday playerBirthday = new PlayerBirthday();

    /**
     * The player's team.
     */
    @Getter protected Team playerTeam = new Team();

    /**
     * The player's position in the game.
     */
    @Getter @Setter protected PlayerPosition playerPosition = new PlayerPosition();

    /**
     * The player's rotation in the game.
     */
    @Getter @Setter protected PlayerPosition playerRotation = new PlayerPosition(0f, 0f, 0f);

    /**
     * The player's level in the game.
     */
    @Getter @Setter protected int playerLevel = 1;

    /**
     * The player's level in their world.
     */
    @Getter @Setter protected int worldLevel = 0;

    /**
     * The player's chat emoji collection.
     */
    @Getter protected Set<Integer> chatEmojiCollection = new HashSet<>();

    /**
     * Shows the player's shown avatars in the profile.
     */
    @Getter @Setter protected boolean showProfileAvatars = false;

    /**
     * The player's friend list.
     */
    @Getter protected Set<Long> friendsList = new HashSet<>();

    /**
     * The player's friend list requests.
     */
    @Getter protected Set<Long> askFriendsList = new HashSet<>();

    /**
     * The player's ignored player list (black list).
     */
    @Getter protected Set<Long> ignoredList = new HashSet<>();

    /**
     * Checks if the player is guest or not.
     * @return True if its guest or else False.
     */
    public abstract boolean isGuest();
}