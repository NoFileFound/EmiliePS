package org.genshinimpact;

// Imports
import java.util.logging.Logger;
import lombok.Getter;
import org.genshinimpact.database.Database;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.utils.GeoIP;
import org.genshinimpact.webserver.SpringBootApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    @Getter private static final Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        initializeWebServer(args);
    }

    public static void initializeWebServer(String[] args) {
        try {
            CryptoUtils.loadDispatchFiles();
            SpringBootApp.main(args);
            GeoIP.loadGeoDatabase();
            Database.initialize();
        } catch (Exception ignored) {}
    }

    public static void initializeGameServer(String[] args) {

    }
}