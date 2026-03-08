package org.rt.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * JDBC 存储实现的单元测试。
 */
class JdbcKvStorageTest {

    /**
     * 验证在本地 H2 数据库上的基本读写删行为。
     */
    @Test
    void shouldPutGetDeleteOnH2() {
        KvStorage storage = new JdbcKvStorage(
                "jdbc:h2:mem:kvtest;MODE=MYSQL;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "rt_kv_store_test"
        );

        storage.put("user:1", "Tom".getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("Tom", new String(storage.get("user:1"), StandardCharsets.UTF_8));

        Assertions.assertTrue(storage.delete("user:1"));
        Assertions.assertNull(storage.get("user:1"));
    }

    /**
     * 验证非法表名会被拒绝。
     */
    @Test
    void shouldRejectInvalidTableName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new JdbcKvStorage(
                        "jdbc:h2:mem:kvtest2;MODE=MYSQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        "",
                        "rt-kv-store"
                )
        );
    }
}