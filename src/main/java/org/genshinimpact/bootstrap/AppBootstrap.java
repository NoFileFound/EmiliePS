package org.genshinimpact.bootstrap;

// Imports
import lombok.Getter;
import org.genshinimpact.MainConfig;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.utils.JsonUtils;

public final class AppBootstrap {
    @Getter private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppBootstrap.class);
    @Getter private static MainConfig mainConfig;
    private static boolean initialized = false;

    public static synchronized void init(boolean dispatch) {
        if(initialized) return;

        try {
            mainConfig = JsonUtils.readFile("config/config.json", MainConfig.class);
            CryptoUtils.loadDispatchFiles(dispatch);
            DBManager.initializeDatabase();
            GeoIP.loadGeoDatabase();
            initialized = true;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}