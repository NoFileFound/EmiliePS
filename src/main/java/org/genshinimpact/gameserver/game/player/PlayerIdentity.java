package org.genshinimpact.gameserver.game.player;

// Imports
import java.util.Map;
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
     * The identity's character list.
     * @return The identity's character (avatar) list.
     */
    Map<Integer, Avatar> getAvatars();

    /**
     * Sets the identity's current avatar id.
     * @param currentAvatarId The current avatar id to set.
     */
    void setCurrentAvatarId(Integer currentAvatarId);

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