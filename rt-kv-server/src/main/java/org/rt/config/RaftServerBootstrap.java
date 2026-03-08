package org.rt.config;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.rt.common.protocol.RaftGroupConstants;
import org.rt.machine.KvStateMachine;
import org.rt.storage.KvStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 通过 Spring 生命周期钩子启动和停止本地 Raft 服务。
 */
@Component
public class RaftServerBootstrap implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(RaftServerBootstrap.class);

    private final KvNodeProperties props;
    private final KvStorage kvStorage;
    private volatile RaftServer raftServer;

    /**
     * 创建引导实例。
     *
     * @param props 节点与集群配置
     * @param kvStorage 已装配的 KV 存储实现
     */
    public RaftServerBootstrap(KvNodeProperties props, KvStorage kvStorage) {
        this.props = props;
        this.kvStorage = kvStorage;
    }

    /**
     * 启动本地 Raft 服务。
     */
    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        try {
            validateConfiguration();
            raftServer = buildRaftServer();
            raftServer.start();
            LOG.info("Raft server started, nodeId={}, storageType={}",
                    props.getNodeId(), kvStorage.getClass().getSimpleName());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Raft server", e);
        }
    }

    /**
     * 停止本地 Raft 服务。
     */
    @Override
    public void stop() {
        RaftServer server = this.raftServer;
        if (server == null) {
            return;
        }

        try {
            server.close();
            LOG.info("Raft server stopped, nodeId={}", props.getNodeId());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to stop Raft server", e);
        } finally {
            this.raftServer = null;
        }
    }

    /**
     * 停止服务并回调 Spring 生命周期钩子。
     *
     * @param callback 停止完成后的回调
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * @return 服务生命周期处于运行态时返回 true
     */
    @Override
    public boolean isRunning() {
        RaftServer server = this.raftServer;
        return server != null && server.getLifeCycleState().isRunning();
    }

    /**
     * @return 返回 true，随 Spring 容器自动启动
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * @return 启动顺序，最大值表示尽量后启动
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * 根据配置构建 RaftServer。
     *
     * @return 可直接启动的 RaftServer 实例
     */
    private RaftServer buildRaftServer() throws IOException {
        RaftPeerId selfId = RaftPeerId.valueOf(props.getNodeId());
        List<RaftPeer> peers = toRaftPeers(props.getPeers());

        RaftGroupId groupId = RaftGroupId.valueOf(RaftGroupConstants.KV_GROUP_UUID);
        RaftGroup group = RaftGroup.valueOf(groupId, peers);

        return RaftServer.newBuilder()
                .setServerId(selfId)
                .setGroup(group)
                .setProperties(new RaftProperties())
                .setStateMachineRegistry(raftGroupId -> new KvStateMachine(kvStorage))
                .build();
    }

    /**
     * 将配置中的节点信息转换为 Ratis 节点对象。
     *
     * @param peerConfigs 配置文件中的节点列表
     * @return Ratis 节点列表
     */
    private List<RaftPeer> toRaftPeers(List<KvNodeProperties.Peer> peerConfigs) {
        return peerConfigs.stream()
                .map(peer -> RaftPeer.newBuilder()
                        .setId(RaftPeerId.valueOf(peer.getId()))
                        .setAddress(peer.getAddress())
                        .build())
                .toList();
    }

    /**
     * 校验引导启动所需配置。
     */
    private void validateConfiguration() {
        if (props.getNodeId() == null || props.getNodeId().isBlank()) {
            throw new IllegalArgumentException("kv.node-id must not be blank");
        }
        if (props.getDataDir() == null || props.getDataDir().isBlank()) {
            throw new IllegalArgumentException("kv.data-dir must not be blank");
        }
        if (props.getPeers() == null || props.getPeers().isEmpty()) {
            throw new IllegalArgumentException("kv.peers must not be empty");
        }
        if (props.getStorage() == null || props.getStorage().getType() == null) {
            throw new IllegalArgumentException("kv.storage.type must not be null");
        }
    }
}
