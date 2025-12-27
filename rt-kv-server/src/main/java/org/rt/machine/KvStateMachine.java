package org.rt.machine;

import org.apache.ratis.protocol.Message;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.rt.storage.KvStorage;

import java.util.concurrent.CompletableFuture;

public class KvStateMachine extends BaseStateMachine {

    private final KvStorage storage;

    public KvStateMachine(KvStorage storage) {
        this.storage = storage;
    }

    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        ByteString data = trx.getLogEntry().getStateMachineLogEntry().getLogData();
        String cmd = data.toStringUtf8();

        if (cmd.startsWith("PUT")) {
            String[] arr = cmd.split(" ", 3);
            storage.put(arr[1], arr[2].getBytes());
            return CompletableFuture.completedFuture(Message.valueOf("OK"));
        }
        if (cmd.startsWith("GET")) {
            String key = cmd.split(" ")[1];
            byte[] v = storage.get(key);
            return CompletableFuture.completedFuture(
                    Message.valueOf(v == null ? "" : new String(v))
            );
        }

        if (cmd.startsWith("DELETE")) {
            String key = cmd.split(" ")[1];
            boolean v = storage.delete(key);
            return CompletableFuture.completedFuture(
                    Message.valueOf(v ? "true" : "false")
            );
        }
        return CompletableFuture.completedFuture(Message.valueOf("UNKNOWN"));
    }
}

