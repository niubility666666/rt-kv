package org.rt.machine;

import org.apache.ratis.protocol.Message;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.rt.common.protocol.KvCommand;
import org.rt.common.protocol.KvCommandCodec;
import org.rt.common.protocol.KvCommandType;
import org.rt.storage.KvStorage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 负责应用复制写请求的 Raft 状态机。
 */
public class KvStateMachine extends BaseStateMachine {

    private static final Message OK = Message.valueOf("OK");

    private final KvStorage storage;

    /**
     * 创建绑定指定存储实现的状态机实例。
     *
     * @param storage KV 存储实现
     */
    public KvStateMachine(KvStorage storage) {
        this.storage = storage;
    }

    /**
     * 应用来自 Raft 日志的复制写事务。
     *
     * @param trx 包含序列化命令的事务上下文
     * @return 命令执行结果
     */
    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        String payload = trx.getLogEntry().getStateMachineLogEntry().getLogData().toStringUtf8();
        KvCommand command = KvCommandCodec.decode(payload);

        return CompletableFuture.completedFuture(applyWrite(command));
    }

    /**
     * 处理线性一致性的只读请求。
     *
     * @param request 序列化后的 GET 命令
     * @return 对应值；键不存在时返回空字符串
     */
    @Override
    public CompletableFuture<Message> query(Message request) {
        KvCommand command = KvCommandCodec.decode(request.getContent().toStringUtf8());
        if (command.type() != KvCommandType.GET) {
            throw new IllegalArgumentException("Only GET is supported in query path");
        }

        byte[] value = storage.get(command.key());
        return CompletableFuture.completedFuture(
                Message.valueOf(value == null ? "" : new String(value, StandardCharsets.UTF_8))
        );
    }

    /**
     * 在存储层执行 PUT 和 DELETE 命令。
     *
     * @param command 解码后的命令
     * @return 写操作结果消息
     */
    private Message applyWrite(KvCommand command) {
        return switch (command.type()) {
            case PUT -> {
                storage.put(command.key(), command.value().getBytes(StandardCharsets.UTF_8));
                yield OK;
            }
            case DELETE -> Message.valueOf(Boolean.toString(storage.delete(command.key())));
            case GET -> throw new IllegalArgumentException("GET should be routed to query path");
        };
    }
}