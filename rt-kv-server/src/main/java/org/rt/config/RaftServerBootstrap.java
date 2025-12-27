package org.rt.config;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.rt.machine.KvStateMachine;
import org.rt.storage.FileLogStorage;
import org.rt.storage.KvStorage;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Component
public class RaftServerBootstrap implements SmartLifecycle {

    private final KvNodeProperties props;
    private RaftServer raftServer;

    public RaftServerBootstrap(KvNodeProperties props) {
        this.props = props;
    }

    @Override
    public void start() {
        try {
            RaftPeerId selfId = RaftPeerId.valueOf(props.getNodeId());

            List<RaftPeer> peers = props.getPeers().stream()
                    .map(p -> RaftPeer.newBuilder()
                            .setId(RaftPeerId.valueOf(p.getId()))
                            .setAddress(p.getAddress())
                            .build())
                    .toList();

            RaftGroupId groupId = RaftGroupId.valueOf(
                    UUID.fromString("12345678-1234-1234-1234-123456789012")
            );

            RaftGroup group = RaftGroup.valueOf(groupId, peers);

            KvStorage storage = new FileLogStorage(Paths.get(props.getDataDir(), props.getNodeId()));

            raftServer = RaftServer.newBuilder()
                    .setServerId(selfId)
                    .setGroup(group)
                    .setProperties(new RaftProperties())
                    .setStateMachineRegistry(gid -> new KvStateMachine(storage))
                    .build();

            raftServer.start();

            System.out.println("Raft Server started: " + selfId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to start RaftServer", e);
        }
    }

    @Override
    public void stop() {
        if (raftServer != null) {
            try {
                raftServer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return raftServer != null && raftServer.getLifeCycleState().isRunning();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}