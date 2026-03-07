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
 * HTTP entrypoint for key-value operations.
 */
@RestController
@RequestMapping("/kv")
public class KvController {

    private final KvService kvService;

    /**
     * Creates controller instance.
     *
     * @param kvService kv service
     */
    public KvController(KvService kvService) {
        this.kvService = kvService;
    }

    /**
     * Stores a key-value pair.
     *
     * @param key key name
     * @param value value content in request body
     * @throws IOException when write fails
     */
    @PostMapping("/{key}")
    public void put(@PathVariable String key, @RequestBody String value) throws IOException {
        kvService.put(key, value);
    }

    /**
     * Reads value by key.
     *
     * @param key key name
     * @return stored value
     * @throws IOException when read fails
     */
    @GetMapping("/{key}")
    public String get(@PathVariable String key) throws IOException {
        return kvService.get(key);
    }

    /**
     * Deletes a key.
     *
     * @param key key name
     * @return true when key is removed
     * @throws IOException when delete fails
     */
    @DeleteMapping("/{key}")
    public boolean delete(@PathVariable String key) throws IOException {
        return kvService.delete(key);
    }
}
