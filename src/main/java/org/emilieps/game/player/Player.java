package org.emilieps.game.player;

// Imports
import lombok.Getter;
import org.emilieps.database.Account;
import org.emilieps.game.GameServer;
import org.emilieps.game.connection.ClientSession;

public class Player {
    @Getter private final Account account;
    @Getter private final int id;
    private final ClientSession session;

    public Player(ClientSession session, Account account) {
        this.account = account;
        this.session = session;
        this.id = 0;
    }


    public GameServer getServer() {
        return this.session.getServer();
    }

    public void logout() {

    }
}