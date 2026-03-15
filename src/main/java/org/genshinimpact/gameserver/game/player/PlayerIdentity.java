package org.genshinimpact.gameserver.game.player;

// Imports
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.genshinimpact.gameserver.game.avatar.Avatar;

public interface PlayerIdentity {
    /**
     * The identity's id.
     * @return The identity's id.
     */
    Long getId();

    /**
     * The identity's username.
     * @return THe identity's username.
     */
    String getUsername();

    /**
     * The identity's token.
     * @return The identity's combo token.
     */
    String getComboToken();

    /**
     * Checks if the identity requires heartbeat.
     * @return True if identity requires heartbeat or else False.
     */
    Boolean getRequireHeartbeat();

    /**
     * Gets the identity last login date.
     * @return The identity's last login date.
     */
    Long getLastLoginDate();

    /**
     * Gets the identity's current avatar id.
     * @return The identity's current avatar id.
     */
    Integer getCurrentAvatarId();

    /**
     * The identity's character list.
     * @return The identity's character (avatar) list.
     */
    Map<Integer, Avatar> getAvatars();

    /**
     * The identity's flycloak list.
     * @return The identity's flycloak list.
     */
    Set<Integer> getFlyCloakList();

    /**
     * The identity's avatar costume list.
     * @return The identity's avatar costume list.
     */
    Set<Integer> getCostumeList();

    /**
     * The identity's team list.
     * @return The identity's team list.
     */
    LinkedHashMap<Integer, PlayerTeam> getTeamList();

    /**
     * The identity's current team id.
     * @return The identity's current team id.
     */
    Integer getCurrentTeamId();

    /**
     * The identity's position in the game.
     * @return The identity's position in the game.
     */
    PlayerPosition getPlayerPosition();

    /**
     * Sets the identity's current avatar id.
     * @param currentAvatarId The current avatar id to set.
     */
    void setCurrentAvatarId(Integer currentAvatarId);

    /**
     * Sets the identity's last login date.
     * @param lastLoginDate The date to set.
     */
    void setLastLoginDate(Long lastLoginDate);

    /**
     * Sets the identity's account nickname.
     * @param username The nickname to set.
     */
    void setUsername(String username);

    /**
     * Saves the player identity (Account / Guest) into the database.
     */
    void save(boolean updateDb);
}