package org.rt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap class for the rt-kv server process.
 */
@SpringBootApplication
public class KvServerApplication {

    /**
     * Starts the Spring container and triggers Raft server bootstrap.
     *
     * @param args startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KvServerApplication.class, args);
    }
}
