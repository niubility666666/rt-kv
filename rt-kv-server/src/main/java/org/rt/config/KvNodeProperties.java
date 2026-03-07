package org.rt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration model for a single Raft node.
 */
@Data
@ConfigurationProperties(prefix = "kv")
public class KvNodeProperties {

    /**
     * Current node id, for example n1.
     */
    private String nodeId;

    /**
     * Local data directory where state files are stored.
     */
    private String dataDir;

    /**
     * Full peer list for the Raft group.
     */
    private List<Peer> peers;

    /**
     * Peer descriptor used in application.yml.
     */
    @Data
    public static class Peer {

        /**
         * Peer id, for example n2.
         */
        private String id;

        /**
         * Peer network address, for example localhost:8082.
         */
        private String address;
    }
}
