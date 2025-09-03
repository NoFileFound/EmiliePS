package org.emilieps;

// Imports
import ch.qos.logback.classic.Logger;
import java.util.Map;
import lombok.Getter;
import org.emilieps.config.*;
import org.emilieps.config.webserver.PlatformExtensionsConfig;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.EmailLib;
import org.emilieps.library.GeoIPLib;
import org.emilieps.library.MongodbLib;
import org.emilieps.library.TranslationLib;

public class Application {
    @Getter private static final Logger logger = (Logger) LoggerFactory.getLogger(Application.class);
    @Getter private static final TranslationLib translations = new TranslationLib();
    @Getter private static final Reflections reflector = new Reflections("org.emilieps");

    // Configs
    @Getter private static ApplicationConfig.ApplicationConfigClass applicationConfig;
    @Getter private static GamePropertiesConfig.GameConfigClass gameConfig;
    @Getter private static HttpPropertiesConfig.HttpConfigClass httpConfig;
    @Getter private static Map<Integer, PlatformExtensionsConfig.PlatformExtensionsClass> deviceExtensionsInfo;

    @SuppressWarnings("unchecked")
    private static void loadConfigVariables() {
        applicationConfig = (ApplicationConfig.ApplicationConfigClass) ConfigLoader.getProperty(ApplicationConfig.class).getInstance();
        gameConfig = (GamePropertiesConfig.GameConfigClass) ConfigLoader.getProperty(GamePropertiesConfig.class).getInstance();
        httpConfig = (HttpPropertiesConfig.HttpConfigClass) ConfigLoader.getProperty(HttpPropertiesConfig.class).getInstance();
        deviceExtensionsInfo = (Map<Integer, PlatformExtensionsConfig.PlatformExtensionsClass>) ConfigLoader.getProperty(PlatformExtensionsConfig.class).getInstance();
    }

    public static void main(String[] args) {
        ConfigLoader.loadConfig();
        loadConfigVariables();
        MongodbLib.initializeDatabase();
        EncryptionLib.loadEncryptionKeys();
        GeoIPLib.loadGeoDatabase();
        EmailLib.initSmtpConfig();
        switch (applicationConfig.applicationType) {
            case HTTP_SERVER -> SpringApp.main(args);
            case GAME_SERVER -> ServerApp.main(args);
            case BOTH -> {
                SpringApp.main(args);
                ServerApp.main(args);
            }
        }
    }
}