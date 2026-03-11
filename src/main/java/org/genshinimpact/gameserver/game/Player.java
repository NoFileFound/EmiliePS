package org.genshinimpact.gameserver.game;

// Imports
import lombok.Getter;
import org.genshinimpact.database.collections.Account;

public class Player {
    @Getter private final Account account;

    public Player(Account account) {
        this.account = account;
    }
}
