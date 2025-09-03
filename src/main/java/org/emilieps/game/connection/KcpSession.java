package org.emilieps.game.connection;

// Imports
import org.emilieps.game.GameServer;
import org.emilieps.game.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface KcpSession extends KcpChannel {
    /**
     * @return The next client sequence to use.
     */
    int getNextClientSequence();

    /**
     * @return The {@link Player} associated with the session, or null if none exists.
     */
    @Nullable Player getPlayer();

    /**
     * @return The game server.
     */
    @NotNull GameServer getServer();

    /**
     * @return The current session's current state.
     */
    @NotNull SessionState getState();

    /**
     * @return The UID of the associated player, or 0 if none exists.
     */
    int getUid();

    /**
     * @return The {@link KcpTunnel} of the session.
     */
    @NotNull KcpTunnel getTunnel();

    /**
     * Sets the state of the session
     * @param state The new {@link SessionState} of the session
     */
    void setState(@NotNull SessionState state);

    /**
     * Sets the {@link Player} associated with the session
     * @param player The new player of the session
     */
    void setPlayer(@Nullable Player player);
}