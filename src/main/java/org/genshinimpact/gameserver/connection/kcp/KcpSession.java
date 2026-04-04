package org.genshinimpact.gameserver.connection.kcp;

// Imports
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.game.Server;
import org.jetbrains.annotations.NotNull;

public interface KcpSession extends KcpChannel {
    /**
     * @return The game server.
     */
    @NotNull Server getServer();

    /**
     * @return The current session's current state.
     */
    @NotNull SessionState getState();

    /**
     * @return The {@link KcpTunnel} of the session.
     */
    @NotNull KcpTunnel getTunnel();

    /**
     * Sets the state of the session
     * @param state The new {@link SessionState} of the session
     */
    void setState(@NotNull SessionState state);
}