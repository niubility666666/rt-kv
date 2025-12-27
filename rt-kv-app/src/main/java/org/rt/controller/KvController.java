package org.rt.controller;

import org.rt.client.KvClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kv")
public class KvController {

    private final KvClient kvClient;

    public KvController(KvClient kvClient) {
        this.kvClient = kvClient;
    }

    @PostMapping("/put")
    public void put(@RequestParam String key,
                    @RequestParam String value) throws Exception {
        kvClient.put(key, value);
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) throws Exception {
        return kvClient.get(key);
    }

    @DeleteMapping("/delete")
    public boolean delete(@RequestParam String key) throws Exception {
        return kvClient.delete(key);
    }
}

