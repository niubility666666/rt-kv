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
 * 面向键值操作的轻量 Raft 客户端封装。
 */
public class KvClient implements Closeable {

    private final RaftClient client;

    /**
     * 创建连接到指定 Raft 节点集合的客户端。
     *
     * @param peers Raft 节点列表
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
     * 通过 Raft 复制链路写入键值对。
     *
     * @param key 键名
     * @param value 值内容
     * @throws IOException RPC 调用失败时抛出
     */
    public void put(String key, String value) throws IOException {
        sendWrite(KvCommand.put(key, value));
    }

    /**
     * 通过只读路径按键读取值。
     *
     * @param key 键名
     * @return 对应值；键不存在时返回空字符串
     * @throws IOException RPC 调用失败时抛出
     */
    public String get(String key) throws IOException {
        RaftClientReply reply = client.io().sendReadOnly(
                Message.valueOf(KvCommandCodec.encode(KvCommand.get(key)))
        );
        return reply.getMessage().getContent().toStringUtf8();
    }

    /**
     * 通过复制写请求删除键。
     *
     * @param key 键名
     * @return 键存在且删除成功时返回 true
     * @throws IOException RPC 调用失败时抛出
     */
    public boolean delete(String key) throws IOException {
        String result = sendWrite(KvCommand.delete(key));
        return Boolean.parseBoolean(result);
    }

    /**
     * 关闭底层 Raft 客户端资源。
     *
     * @throws IOException 关闭失败时抛出
     */
    @Override
    public void close() throws IOException {
        client.close();
    }

    /**
     * 通过复制写路径发送状态变更命令。
     *
     * @param command 写命令
     * @return 状态机返回结果
     * @throws IOException RPC 调用失败时抛出
     */
    private String sendWrite(KvCommand command) throws IOException {
        RaftClientReply reply = client.io().send(Message.valueOf(KvCommandCodec.encode(command)));
        return reply.getMessage().getContent().toStringUtf8();
    }
}