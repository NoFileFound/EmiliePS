package org.genshinimpact.bootstrap;

// Imports
import lombok.Getter;
import org.genshinimpact.MainConfig;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.utils.JsonUtils;

public final class AppBootstrap {
    @Getter private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppBootstrap.class);
    @Getter private static MainConfig mainConfig;
    private static boolean initialized = false;

    /**
     * Initializes the server and its pre-required files.
     * @param state The application startup type.
     */
    public static synchronized void init(int state) {
        if(initialized) return;

        try {
            mainConfig = JsonUtils.readFile("config/config.json", MainConfig.class);
            CryptoUtils.loadDispatchFiles(state);
            DBManager.initializeDatabase();
            GeoIP.loadGeoDatabase();
            initialized = true;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shutdowns the server.
     */
    public static synchronized void stopServer() {
        AppBootstrap.getLogger().info("Shutdown-ing the server...");
        DBManager.shutdownDatabase();
    }
}