package org.rt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration model for kv client peer discovery.
 */
@Data
@ConfigurationProperties(prefix = "kv")
public class KvClientProperties {

    /**
     * Peer list for client failover and routing.
     */
    private List<Peer> peers;

    /**
     * Peer descriptor used in application.yml.
     */
    @Data
    public static class Peer {

        /**
         * Peer id, for example n1.
         */
        private String id;

        /**
         * Peer address, for example localhost:8081.
         */
        private String address;
    }
}
