package org.genshinimpact.gameserver.game.account;

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
     * Sets the identity's account nickname.
     * @param username The nickname to set.
     */
    void setUsername(String username);

    /**
     * Saves the player identity (Account / Guest) into the database.
     */
    void save(boolean updateDb);
}