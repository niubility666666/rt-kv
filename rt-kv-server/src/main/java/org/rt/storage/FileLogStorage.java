package org.rt.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * 基于文件系统的 {@link KvStorage} 实现。
 *
 * <p>每个键会映射为数据目录下的 Base64 文件名。
 */
public class FileLogStorage implements KvStorage {

    private static final String FILE_SUFFIX = ".kv";
    private static final Base64.Encoder FILE_NAME_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final Path dir;

    /**
     * 在指定目录下创建存储实例。
     *
     * @param dir 数据目录
     * @throws IOException 目录创建失败时抛出
     */
    public FileLogStorage(Path dir) throws IOException {
        this.dir = dir;
        Files.createDirectories(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, byte[] value) {
        Path file = resolveKeyFile(key);
        try {
            Files.write(file, value);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(String key) {
        Path file = resolveKeyFile(key);
        try {
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String key) {
        Path file = resolveKeyFile(key);
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete key: " + key, e);
        }
    }

    /**
     * 将用户键转换为安全的本地文件路径。
     *
     * @param key 逻辑键名
     * @return 物理文件路径
     */
    private Path resolveKeyFile(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }

        String fileName = FILE_NAME_ENCODER.encodeToString(key.getBytes(StandardCharsets.UTF_8)) + FILE_SUFFIX;
        return dir.resolve(fileName);
    }
}