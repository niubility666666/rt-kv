package org.rt.service;

import org.rt.client.KvClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 对底层 {@link KvClient} 的应用服务封装。
 */
@Service
public class KvService {

    private final KvClient kvClient;

    /**
     * 创建服务实例。
     *
     * @param kvClient Raft KV 客户端
     */
    public KvService(KvClient kvClient) {
        this.kvClient = kvClient;
    }

    /**
     * 写入键值对。
     *
     * @param key 键名
     * @param value 值内容
     * @throws IOException 写入失败时抛出
     */
    public void put(String key, String value) throws IOException {
        kvClient.put(key, value);
    }

    /**
     * 按键读取值。
     *
     * @param key 键名
     * @return 值字符串
     * @throws IOException 读取失败时抛出
     */
    public String get(String key) throws IOException {
        return kvClient.get(key);
    }

    /**
     * 删除键。
     *
     * @param key 键名
     * @return 删除成功时返回 true
     * @throws IOException 删除失败时抛出
     */
    public boolean delete(String key) throws IOException {
        return kvClient.delete(key);
    }
}