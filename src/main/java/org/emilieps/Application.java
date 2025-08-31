package org.emilieps;

// Imports
import ch.qos.logback.classic.Logger;
import java.util.Map;
import lombok.Getter;
import org.emilieps.bootspring.SpringApp;
import org.emilieps.game.ServerApp;
import org.emilieps.properties.configs.PlatformExtensionsConfig;
import org.emilieps.properties.configs.PropertiesConfig;
import org.emilieps.properties.configs.RegionConfig;
import org.emilieps.database.DBManager;
import org.emilieps.libraries.EncryptionManager;
import org.emilieps.libraries.TranslationManager;
import org.emilieps.properties.ConfigLoader;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

public class Application {
    @Getter private static final Logger logger = (Logger) LoggerFactory.getLogger(Application.class);
    @Getter private static final TranslationManager translationManager = new TranslationManager();
    @Getter private static final Reflections reflector = new Reflections("org.emilieps");

    // Configs
    @Getter private static PropertiesConfig.PropertiesClass propertiesInfo;
    @Getter private static RegionConfig.RegionConfigClass regionInfo;
    @Getter private static Map<Integer, PlatformExtensionsConfig.PlatformExtensionsClass> deviceExtensionsInfo;

    @SuppressWarnings("unchecked")
    private static void loadConfigVariables() {
        propertiesInfo = (PropertiesConfig.PropertiesClass) ConfigLoader.getProperty(PropertiesConfig.class).getInstance();
        regionInfo = (RegionConfig.RegionConfigClass) ConfigLoader.getProperty(RegionConfig.class).getInstance();
        deviceExtensionsInfo = (Map<Integer, PlatformExtensionsConfig.PlatformExtensionsClass>) ConfigLoader.getProperty(PlatformExtensionsConfig.class).getInstance();
    }

    public static void main(String[] args) {
        ConfigLoader.loadConfig();
        loadConfigVariables();
        DBManager.initializeDatabase();
        EncryptionManager.loadEncryptionKeys();

        SpringApp.main(args);
        ServerApp.main(args);
    }
}