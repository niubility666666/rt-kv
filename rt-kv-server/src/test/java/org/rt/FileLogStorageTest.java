package org.rt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rt.storage.FileLogStorage;
import org.rt.storage.KvStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Unit tests for file-based storage.
 */
class FileLogStorageTest {

    /**
     * Temporary directory managed by JUnit.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifies put/get/delete behavior.
     *
     * @throws Exception when storage initialization fails
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
