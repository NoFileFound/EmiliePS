package org.genshinimpact;

// Imports
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.webserver.SpringBootApp;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        int startType = 2;
        AppBootstrap.init(startType == 0 || startType == 2);
        if(startType == 2) {
            SpringBootApp.main(args);
        }
    }
}