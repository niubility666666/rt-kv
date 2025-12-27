package org.rt.storage;

public interface KvStorage {
    void put(String key, byte[] value);
    byte[] get(String key);
    boolean delete(String key);
}
