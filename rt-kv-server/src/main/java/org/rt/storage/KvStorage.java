package org.rt.storage;

/**
 * Storage abstraction for key-value state persistence.
 */
public interface KvStorage {

    /**
     * Stores or overwrites a key-value pair.
     *
     * @param key key name
     * @param value raw value bytes
     */
    void put(String key, byte[] value);

    /**
     * Reads value bytes by key.
     *
     * @param key key name
     * @return value bytes, or {@code null} when key does not exist
     */
    byte[] get(String key);

    /**
     * Deletes a key if it exists.
     *
     * @param key key name
     * @return true when a key is deleted
     */
    boolean delete(String key);
}
