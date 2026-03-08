package org.rt.storage;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 基于 Redis 的键值存储实现。
 */
public class RedisKvStorage implements KvStorage, DisposableBean {

    private final LettuceConnectionFactory connectionFactory;
    private final String keyPrefix;

    /**
     * 创建 Redis 存储实例。
     *
     * @param host Redis 主机
     * @param port Redis 端口
     * @param password Redis 密码
     * @param database Redis DB 索引
     * @param keyPrefix Key 前缀
     */
    public RedisKvStorage(String host, int port, String password, int database, String keyPrefix) {
        String normalizedHost = requireText(host, "host");
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than zero");
        }
        if (database < 0) {
            throw new IllegalArgumentException("database must be greater than or equal to zero");
        }

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(normalizedHost, port);
        configuration.setDatabase(database);
        if (password != null && !password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }

        this.connectionFactory = new LettuceConnectionFactory(configuration);
        this.connectionFactory.afterPropertiesSet();
        this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, byte[] value) {
        requireText(key, "key");
        Objects.requireNonNull(value, "value must not be null");

        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.stringCommands().set(rawKey(key), value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(String key) {
        requireText(key, "key");

        try (RedisConnection connection = connectionFactory.getConnection()) {
            return connection.stringCommands().get(rawKey(key));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String key) {
        requireText(key, "key");

        try (RedisConnection connection = connectionFactory.getConnection()) {
            Long deleted = connection.keyCommands().del(rawKey(key));
            return deleted != null && deleted > 0;
        }
    }

    /**
     * 关闭 Redis 连接工厂资源。
     */
    @Override
    public void destroy() {
        connectionFactory.destroy();
    }

    /**
     * 构造带前缀的 Redis Key。
     *
     * @param key 业务键名
     * @return Redis Key 字节
     */
    private byte[] rawKey(String key) {
        return (keyPrefix + key).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 校验并返回非空文本。
     *
     * @param value 文本值
     * @param field 字段名
     * @return 非空文本
     */
    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}