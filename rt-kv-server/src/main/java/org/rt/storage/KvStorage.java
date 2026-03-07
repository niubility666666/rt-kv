package org.rt.storage;

/**
 * 键值状态持久化的存储抽象。
 */
public interface KvStorage {

    /**
     * 存储或覆盖一个键值对。
     *
     * @param key 键名
     * @param value 原始值字节数组
     */
    void put(String key, byte[] value);

    /**
     * 按键读取值字节数组。
     *
     * @param key 键名
     * @return 值字节数组；键不存在时返回 {@code null}
     */
    byte[] get(String key);

    /**
     * 删除指定键（若存在）。
     *
     * @param key 键名
     * @return 删除成功时返回 true
     */
    boolean delete(String key);
}