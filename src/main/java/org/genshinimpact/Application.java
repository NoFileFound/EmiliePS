package org.genshinimpact;

// Imports
import org.genshinimpact.bootstrap.AppBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    /**
     * Entry point of the application.
     * @param args the command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        AppBootstrap.init(2, args);
    }
}