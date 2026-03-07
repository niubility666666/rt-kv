package org.rt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap class for the demo http application.
 */
@SpringBootApplication
public class KvAppApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KvAppApplication.class, args);
    }
}
