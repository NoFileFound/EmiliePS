package org.emilieps;

// Imports
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringApp {
    /**
     * Starts the springboot application (http/web server).
     * @param args The console arguments.
     */
    public static void main(String[] args) {
        new SpringApplication(SpringApp.class).run(args);
    }
}