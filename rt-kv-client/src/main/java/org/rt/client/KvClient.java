package org.rt.client;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class KvClient implements Closeable {

    private final RaftClient client;

    public KvClient(List<RaftPeer> peers) {
        RaftGroup group = RaftGroup.valueOf(
                RaftGroupId.randomId(), peers
        );

        this.client = RaftClient.newBuilder()
                .setRaftGroup(group)
                .build();
    }

    public void put(String key, String value) throws Exception {
        client.io().send(
                Message.valueOf("PUT " + key + " " + value)
        );
    }

    public String get(String key) throws Exception {
        Message msg = client.io().sendReadOnly(
                Message.valueOf("GET " + key)
        ).getMessage();
        return msg.getContent().toStringUtf8();
    }

    public boolean delete(String key) throws Exception {
        Message msg = client.io().sendReadOnly(
                Message.valueOf("DELETE " + key)
        ).getMessage();
        return Boolean.parseBoolean(msg.getContent().toStringUtf8());
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}

