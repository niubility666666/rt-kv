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
 * Raft state machine that applies replicated kv write operations.
 */
public class KvStateMachine extends BaseStateMachine {

    private static final Message OK = Message.valueOf("OK");

    private final KvStorage storage;

    /**
     * Creates a state machine instance bound to the provided storage backend.
     *
     * @param storage kv storage implementation
     */
    public KvStateMachine(KvStorage storage) {
        this.storage = storage;
    }

    /**
     * Applies replicated write transactions from Raft logs.
     *
     * @param trx transaction context containing serialized command
     * @return command execution result
     */
    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        String payload = trx.getLogEntry().getStateMachineLogEntry().getLogData().toStringUtf8();
        KvCommand command = KvCommandCodec.decode(payload);

        return CompletableFuture.completedFuture(applyWrite(command));
    }

    /**
     * Handles linearizable read-only requests.
     *
     * @param request serialized get command
     * @return value payload or empty string when key does not exist
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
     * Executes put and delete commands against storage.
     *
     * @param command decoded command
     * @return write result message
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
