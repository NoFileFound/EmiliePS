package org.genshinhttpsrv;

// Imports
import ch.qos.logback.classic.Logger;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.genshinhttpsrv.database.DBManager;
import org.genshinhttpsrv.libraries.EncryptionManager;
import org.genshinhttpsrv.libraries.TranslationManager;
import org.genshinhttpsrv.properties.ConfigLoader;
import org.genshinhttpsrv.properties.configs.*;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {
    @Getter private static final Logger logger = (Logger) LoggerFactory.getLogger(Application.class);
    @Getter private static final TranslationManager translationManager = new TranslationManager();
    @Getter private static final Reflections reflector = new Reflections("org.genshinhttpsrv.properties.configs");

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

    @RequestMapping("/**")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUnknownRequest(HttpServletRequest request) {
        logger.warn("Unimplemented endpoint called: {} {}", request.getMethod(), request.getRequestURI());
        return "Endpoint not implemented: " + request.getRequestURI();
    }

    public static void main(String[] args) {
        ConfigLoader.loadConfig();
        loadConfigVariables();
        DBManager.initializeDatabase();
        EncryptionManager.loadEncryptionKeys();

        SpringApplication app = new SpringApplication(Application.class);
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("server.port", 8881);

        app.setDefaultProperties(defaultProperties);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }
}