package org.genshinimpact.gameserver;

// Imports
import java.io.IOException;
import lombok.Getter;
import org.genshinimpact.configs.GameConfig;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.webserver.utils.JsonUtils;

public final class ServerApp {
    @Getter private static final GameConfig gameConfig;
    @Getter private static Server gameServer;

    static {
        try {
            gameConfig = JsonUtils.readFile("config/gameserver.json", GameConfig.class);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The main function to start the gameserver.
     * @param args The command arguments if there are any.
     */
    public static void main(String[] args) {
        gameServer = new Server();
    }
}