package org.rt.config;

import org.rt.client.KvClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KvTestRunner implements CommandLineRunner {

    private final KvClient kvClient;

    public KvTestRunner(KvClient kvClient) {
        this.kvClient = kvClient;
    }

    @Override
    public void run(String... args) throws Exception {
        kvClient.put("user:1", "Tom");
        System.out.println(kvClient.get("user:1"));
    }
}
