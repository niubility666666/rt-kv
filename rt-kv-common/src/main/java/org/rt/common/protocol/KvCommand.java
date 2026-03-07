package org.rt.common.protocol;

import java.util.Objects;

/**
 * Immutable command object exchanged between client and state machine.
 */
public final class KvCommand {

    private final KvCommandType type;
    private final String key;
    private final String value;

    private KvCommand(KvCommandType type, String key, String value) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.key = requireText(key, "key");
        this.value = value;

        if (type == KvCommandType.PUT) {
            requireText(value, "value");
        }
    }

    /**
     * Creates a put command.
     *
     * @param key key name
     * @param value value content
     * @return command instance
     */
    public static KvCommand put(String key, String value) {
        return new KvCommand(KvCommandType.PUT, key, value);
    }

    /**
     * Creates a get command.
     *
     * @param key key name
     * @return command instance
     */
    public static KvCommand get(String key) {
        return new KvCommand(KvCommandType.GET, key, null);
    }

    /**
     * Creates a delete command.
     *
     * @param key key name
     * @return command instance
     */
    public static KvCommand delete(String key) {
        return new KvCommand(KvCommandType.DELETE, key, null);
    }

    /**
     * @return command type
     */
    public KvCommandType type() {
        return type;
    }

    /**
     * @return command key
     */
    public String key() {
        return key;
    }

    /**
     * @return command value, available for put command
     */
    public String value() {
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
