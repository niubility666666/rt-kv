package org.rt.controller;

import org.rt.service.KvService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 键值操作的 HTTP 接口入口。
 */
@RestController
@RequestMapping("/kv")
public class KvController {

    private final KvService kvService;

    /**
     * 创建控制器实例。
     *
     * @param kvService KV 服务
     */
    public KvController(KvService kvService) {
        this.kvService = kvService;
    }

    /**
     * 写入一个键值对。
     *
     * @param key 键名
     * @param value 请求体中的值内容
     * @throws IOException 写入失败时抛出
     */
    @PostMapping("/{key}")
    public void put(@PathVariable String key, @RequestBody String value) throws IOException {
        kvService.put(key, value);
    }

    /**
     * 按键读取值。
     *
     * @param key 键名
     * @return 存储的值
     * @throws IOException 读取失败时抛出
     */
    @GetMapping("/{key}")
    public String get(@PathVariable String key) throws IOException {
        return kvService.get(key);
    }

    /**
     * 删除一个键。
     *
     * @param key 键名
     * @return 删除成功时返回 true
     * @throws IOException 删除失败时抛出
     */
    @DeleteMapping("/{key}")
    public boolean delete(@PathVariable String key) throws IOException {
        return kvService.delete(key);
    }
}