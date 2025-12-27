package org.rt;

import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.junit.jupiter.api.Test;
import org.rt.machine.KvStateMachine;
import org.rt.storage.FileLogStorage;
import org.rt.storage.KvStorage;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class KvServer {

    @Test
    public void test() {
        RaftPeerId id = RaftPeerId.valueOf(args[0]);

        List<RaftPeer> peers = List.of(
                new RaftPeer(RaftPeerId.valueOf("n1"), "localhost:8081"),
                new RaftPeer(RaftPeerId.valueOf("n2"), "localhost:8082"),
                new RaftPeer(RaftPeerId.valueOf("n3"), "localhost:8083")
        );

        RaftGroup group = RaftGroup.valueOf(
                RaftGroupId.randomId(), peers
        );

        KvStorage storage = new FileLogStorage(
                Paths.get("data", id.toString())
        );

        RaftServer server = RaftServer.newBuilder()
                .setServerId(id)
                .setGroup(group)
                .setStateMachine(new KvStateMachine(storage))
                .setProperties(new RaftProperties())
                .build();

        server.start();
    }
}
