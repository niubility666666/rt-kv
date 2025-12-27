package org.rt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "kv")
@Data
public class KvClientProperties {

    private List<Peer> peers;

    @Data
    public static class Peer {
        private String id;
        private String address;
    }
}

