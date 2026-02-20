package org.genshinimpact.webserver;

// Imports
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.genshinimpact.utils.JsonUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class, org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class})
public class SpringBootApp {
    @Getter private static final WebConfig webConfig;

    static {
        try {
            webConfig = JsonUtils.readFile("config/webserver.json", WebConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The main function to start the webserver.
     * @param args The command arguments if there are any.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringBootApp.class);
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("server.port", webConfig.springBootConfig.springbootPort);
        defaultProperties.put("logging.level.root", webConfig.springBootConfig.springbootLogLevel);
        defaultProperties.put("logging.level.org.springframework", webConfig.springBootConfig.springbootLogLevel);
        defaultProperties.put("server.compression.enabled", webConfig.springBootConfig.springbootEnableCompression);
        defaultProperties.put("spring.web.locale", "ja_JP");
        app.setDefaultProperties(defaultProperties);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.run(args);
    }
}