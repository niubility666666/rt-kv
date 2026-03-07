package org.rt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rt.storage.FileLogStorage;
import org.rt.storage.KvStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 文件存储实现的单元测试。
 */
class FileLogStorageTest {

    /**
     * JUnit 管理的临时目录。
     */
    @TempDir
    Path tempDir;

    /**
     * 验证 put/get/delete 行为。
     *
     * @throws Exception 存储初始化失败时抛出
     */
    @Test
    void shouldPutGetDelete() throws Exception {
        KvStorage storage = new FileLogStorage(tempDir);
        storage.put("user:1", "Tom".getBytes(StandardCharsets.UTF_8));

        Assertions.assertEquals("Tom", new String(storage.get("user:1"), StandardCharsets.UTF_8));
        Assertions.assertTrue(storage.delete("user:1"));
        Assertions.assertNull(storage.get("user:1"));
    }
}