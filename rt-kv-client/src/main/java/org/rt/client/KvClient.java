package org.rt.client;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.rt.common.protocol.KvCommand;
import org.rt.common.protocol.KvCommandCodec;
import org.rt.common.protocol.RaftGroupConstants;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Thin Raft client wrapper for key-value operations.
 */
public class KvClient implements Closeable {

    private final RaftClient client;

    /**
     * Creates a client connected to the configured Raft peer set.
     *
     * @param peers raft peer list
     */
    public KvClient(List<RaftPeer> peers) {
        RaftGroup group = RaftGroup.valueOf(
                RaftGroupId.valueOf(RaftGroupConstants.KV_GROUP_UUID),
                peers
        );

        this.client = RaftClient.newBuilder()
                .setRaftGroup(group)
                .build();
    }

    /**
     * Writes a key-value pair through Raft replication.
     *
     * @param key key name
     * @param value value content
     * @throws IOException when rpc fails
     */
    public void put(String key, String value) throws IOException {
        sendWrite(KvCommand.put(key, value));
    }

    /**
     * Reads a value by key using read-only path.
     *
     * @param key key name
     * @return value content, empty string when key not found
     * @throws IOException when rpc fails
     */
    public String get(String key) throws IOException {
        RaftClientReply reply = client.io().sendReadOnly(
                Message.valueOf(KvCommandCodec.encode(KvCommand.get(key)))
        );
        return reply.getMessage().getContent().toStringUtf8();
    }

    /**
     * Deletes a key via replicated write request.
     *
     * @param key key name
     * @return true when key existed and was deleted
     * @throws IOException when rpc fails
     */
    public boolean delete(String key) throws IOException {
        String result = sendWrite(KvCommand.delete(key));
        return Boolean.parseBoolean(result);
    }

    /**
     * Closes underlying Raft client resources.
     *
     * @throws IOException when close fails
     */
    @Override
    public void close() throws IOException {
        client.close();
    }

    /**
     * Sends a state-changing command through replicated write path.
     *
     * @param command write command
     * @return response payload from state machine
     * @throws IOException when rpc fails
     */
    private String sendWrite(KvCommand command) throws IOException {
        RaftClientReply reply = client.io().send(Message.valueOf(KvCommandCodec.encode(command)));
        return reply.getMessage().getContent().toStringUtf8();
    }
}
