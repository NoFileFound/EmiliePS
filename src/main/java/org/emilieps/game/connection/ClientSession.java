package org.emilieps.game.connection;

// Imports
import lombok.Getter;
import lombok.Setter;
import org.emilieps.game.Server;
import org.kcp.Ukcp;

public class ClientSession {
    @Getter private final String ipAddress;
    @Getter @Setter private SessionState sessionState;
    private final Server server;
    private final Ukcp protocol;

    /**
     * Creates a new session in the game server.
     * @param server The game server.
     * @param protocol The client's kcp protocol.
     */
    public ClientSession(Server server, Ukcp protocol) {
        this.server = server;
        this.protocol = protocol;
        this.ipAddress = protocol.user().getRemoteAddress().getAddress().getHostAddress();
        this.sessionState = SessionState.INACTIVE;
    }

    /**
     * Closes the client's connection.
     */
    public void closeConnection() {
        this.protocol.close();
    }
}