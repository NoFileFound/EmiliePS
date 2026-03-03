package org.genshinimpact.bootstrap;

// Imports
import lombok.Getter;
import org.genshinimpact.configs.MainConfig;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.gameserver.ServerApp;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.genshinimpact.webserver.utils.SMTPUtils;
import org.reflections.Reflections;
import org.slf4j.bridge.SLF4JBridgeHandler;

public final class AppBootstrap {
    @Getter private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppBootstrap.class);
    @Getter private static final Reflections reflector = new Reflections("org.genshinimpact.gameserver.packets");
    @Getter private static MainConfig mainConfig;
    private static boolean initialized = false;

    /**
     * Initializes the server and its pre-required files.
     */
    public static synchronized void init(String[] args) {
        if(initialized) return;

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        try {
            mainConfig = JsonUtils.readFile("config/config.json", MainConfig.class);
            CryptoUtils.loadDispatchFiles();
            DBManager.initializeDatabase();
            GeoIP.loadGeoDatabase();
            SMTPUtils.initSmtpConfig();
            SpringBootApp.main(args);
            ServerApp.main(args);

            initialized = true;
            Runtime.getRuntime().addShutdownHook(new Thread(AppBootstrap::stopServer));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shutdowns the server.
     */
    public static synchronized void stopServer() {
        AppBootstrap.getLogger().info("Shutdown-ing the server...");
        ServerApp.getGameServer().shutdownServer();
        DBManager.shutdownDatabase();
    }
}