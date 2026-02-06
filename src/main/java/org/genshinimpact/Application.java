package org.genshinimpact;

// Imports
import java.util.logging.Logger;
import lombok.Getter;
import org.genshinimpact.database.Database;
import org.genshinimpact.libraries.GeoIP;
import org.genshinimpact.webserver.SpringBootApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    @Getter private static final Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        SpringBootApp.main(args);
        GeoIP.loadGeoDatabase();
        Database.initialize();
    }
}