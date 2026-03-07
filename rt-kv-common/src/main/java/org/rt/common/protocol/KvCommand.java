package org.rt.common.protocol;

import java.util.Objects;

/**
 * 在客户端与状态机之间传输的不可变命令对象。
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
     * 创建 PUT 命令。
     *
     * @param key 键名
     * @param value 值内容
     * @return 命令实例
     */
    public static KvCommand put(String key, String value) {
        return new KvCommand(KvCommandType.PUT, key, value);
    }

    /**
     * 创建 GET 命令。
     *
     * @param key 键名
     * @return 命令实例
     */
    public static KvCommand get(String key) {
        return new KvCommand(KvCommandType.GET, key, null);
    }

    /**
     * 创建 DELETE 命令。
     *
     * @param key 键名
     * @return 命令实例
     */
    public static KvCommand delete(String key) {
        return new KvCommand(KvCommandType.DELETE, key, null);
    }

    /**
     * @return 命令类型
     */
    public KvCommandType type() {
        return type;
    }

    /**
     * @return 命令键
     */
    public String key() {
        return key;
    }

    /**
     * @return 命令值，仅 PUT 命令有效
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