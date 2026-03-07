package org.rt.config;

import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.rt.client.KvClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Auto configuration that exposes {@link KvClient} as Spring bean.
 */
@Configuration
public class KvClientAutoConfiguration {

    /**
     * Builds a singleton kv client from configured peer list.
     *
     * @param props peer properties
     * @return kv client bean
     */
    @Bean(destroyMethod = "close")
    public KvClient kvClient(KvClientProperties props) {
        validateProperties(props);
        return new KvClient(toRaftPeers(props));
    }

    /**
     * Converts application peers to Ratis peers.
     *
     * @param props client properties
     * @return raft peers
     */
    private List<RaftPeer> toRaftPeers(KvClientProperties props) {
        return props.getPeers().stream()
                .map(peer -> RaftPeer.newBuilder()
                        .setId(RaftPeerId.valueOf(peer.getId()))
                        .setAddress(peer.getAddress())
                        .build())
                .toList();
    }

    /**
     * Validates required client properties.
     *
     * @param props client properties
     */
    private void validateProperties(KvClientProperties props) {
        if (props.getPeers() == null || props.getPeers().isEmpty()) {
            throw new IllegalArgumentException("kv.peers must not be empty");
        }
    }
}
