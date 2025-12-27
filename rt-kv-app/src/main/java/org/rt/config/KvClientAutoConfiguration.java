package org.rt.config;

import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.rt.client.KvClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class KvClientAutoConfiguration {

    @Bean(destroyMethod = "close")
    public KvClient kvClient(KvClientProperties props) {

        List<RaftPeer> peers = props.getPeers().stream()
                .map(p -> RaftPeer.newBuilder()
                        .setId(RaftPeerId.valueOf(p.getId()))
                        .setAddress(p.getAddress())
                        .build()
                )
                .toList();

        return new KvClient(peers);
    }
}

