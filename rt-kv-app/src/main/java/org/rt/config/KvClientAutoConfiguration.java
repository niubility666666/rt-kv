package org.rt.config;

import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.rt.client.KvClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 将 {@link KvClient} 装配为 Spring Bean 的自动配置。
 */
@Configuration
public class KvClientAutoConfiguration {

    /**
     * 根据配置节点列表构建单例 KV 客户端。
     *
     * @param props 节点配置属性
     * @return KV 客户端 Bean
     */
    @Bean(destroyMethod = "close")
    public KvClient kvClient(KvClientProperties props) {
        validateProperties(props);
        return new KvClient(toRaftPeers(props));
    }

    /**
     * 将应用配置节点转换为 Ratis 节点对象。
     *
     * @param props 客户端配置属性
     * @return Raft 节点列表
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
     * 校验客户端必要配置。
     *
     * @param props 客户端配置属性
     */
    private void validateProperties(KvClientProperties props) {
        if (props.getPeers() == null || props.getPeers().isEmpty()) {
            throw new IllegalArgumentException("kv.peers must not be empty");
        }
    }
}