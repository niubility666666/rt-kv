package org.rt.config;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.rt.common.protocol.RaftGroupConstants;
import org.rt.machine.KvStateMachine;
import org.rt.storage.FileLogStorage;
import org.rt.storage.KvStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Starts and stops the local Raft server with Spring lifecycle hooks.
 */
@Component
public class RaftServerBootstrap implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(RaftServerBootstrap.class);

    private final KvNodeProperties props;
    private volatile RaftServer raftServer;

    /**
     * Creates bootstrap instance.
     *
     * @param props node and peer configuration
     */
    public RaftServerBootstrap(KvNodeProperties props) {
        this.props = props;
    }

    /**
     * Starts the local Raft server.
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
            LOG.info("Raft server started, nodeId={}, dataDir={}", props.getNodeId(), props.getDataDir());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Raft server", e);
        }
    }

    /**
     * Stops the local Raft server.
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
     * Stops server and invokes Spring callback.
     *
     * @param callback callback to notify completion
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * @return true when server lifecycle is running
     */
    @Override
    public boolean isRunning() {
        RaftServer server = this.raftServer;
        return server != null && server.getLifeCycleState().isRunning();
    }

    /**
     * @return true so server auto starts with Spring context
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * @return startup order, max value to start after most beans
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * Builds RaftServer from configuration.
     *
     * @return ready-to-start Raft server
     * @throws IOException when storage init fails
     */
    private RaftServer buildRaftServer() throws IOException {
        RaftPeerId selfId = RaftPeerId.valueOf(props.getNodeId());
        List<RaftPeer> peers = toRaftPeers(props.getPeers());

        RaftGroupId groupId = RaftGroupId.valueOf(RaftGroupConstants.KV_GROUP_UUID);
        RaftGroup group = RaftGroup.valueOf(groupId, peers);

        KvStorage storage = new FileLogStorage(Paths.get(props.getDataDir(), props.getNodeId()));

        return RaftServer.newBuilder()
                .setServerId(selfId)
                .setGroup(group)
                .setProperties(new RaftProperties())
                .setStateMachineRegistry(raftGroupId -> new KvStateMachine(storage))
                .build();
    }

    /**
     * Converts config peers to Ratis peers.
     *
     * @param peerConfigs configured peers
     * @return Ratis peer list
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
     * Validates required bootstrap properties.
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
    }
}
