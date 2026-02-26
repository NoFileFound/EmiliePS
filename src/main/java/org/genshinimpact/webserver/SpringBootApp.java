package org.genshinimpact.webserver;

// Imports
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.genshinimpact.webserver.services.HeartbeatService;
import org.genshinimpact.webserver.stores.CrashLogStore;
import org.genshinimpact.webserver.stores.GeetestStore;
import org.genshinimpact.webserver.stores.TicketStore;
import org.genshinimpact.webserver.utils.JsonUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class, org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class})
public class SpringBootApp {
    @Getter private static final WebConfig webConfig;
    @Getter private static final GeetestStore captchaStore;
    @Getter private static final CrashLogStore crashLogStore;
    @Getter private static final TicketStore ticketStore;
    @Getter private static final HeartbeatService heartbeatService;

    static {
        try {
            webConfig = JsonUtils.readFile("config/webserver.json", WebConfig.class);
            captchaStore = new GeetestStore();
            crashLogStore = new CrashLogStore();
            ticketStore = new TicketStore();
            heartbeatService = new HeartbeatService();
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
        defaultProperties.put("server.ssl.enabled", webConfig.springBootConfig.springbootEnableSSL);
        if(webConfig.springBootConfig.springbootEnableSSL) {
            defaultProperties.put("server.ssl.key-store", "classpath:webserver/keystore.p12");
            defaultProperties.put("server.ssl.key-store-password", webConfig.springBootConfig.springbootEnableSSLPassword);
            defaultProperties.put("server.ssl.key-store-type", "PKCS12");
            defaultProperties.put("server.ssl.key-alias", "tomcat");
            defaultProperties.put("server.ssl.enabled-protocols", "TLSv1.2,TLSv1.3");
        }

        app.setDefaultProperties(defaultProperties);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.run(args);
    }
}