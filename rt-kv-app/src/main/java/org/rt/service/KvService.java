package org.rt.service;

import org.rt.client.KvClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Application service that wraps low-level {@link KvClient} operations.
 */
@Service
public class KvService {

    private final KvClient kvClient;

    /**
     * Creates service instance.
     *
     * @param kvClient raft kv client
     */
    public KvService(KvClient kvClient) {
        this.kvClient = kvClient;
    }

    /**
     * Writes key-value pair.
     *
     * @param key key name
     * @param value value content
     * @throws IOException when write fails
     */
    public void put(String key, String value) throws IOException {
        kvClient.put(key, value);
    }

    /**
     * Reads value by key.
     *
     * @param key key name
     * @return value string
     * @throws IOException when read fails
     */
    public String get(String key) throws IOException {
        return kvClient.get(key);
    }

    /**
     * Deletes key.
     *
     * @param key key name
     * @return true when key is removed
     * @throws IOException when delete fails
     */
    public boolean delete(String key) throws IOException {
        return kvClient.delete(key);
    }
}
