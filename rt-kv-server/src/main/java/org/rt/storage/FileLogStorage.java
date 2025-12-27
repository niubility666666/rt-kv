package org.rt.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileLogStorage implements KvStorage {

    private final Path dir;

    public FileLogStorage(Path dir) throws IOException {
        this.dir = dir;
        Files.createDirectories(dir);
    }

    @Override
    public void put(String key, byte[] value) {
        try {
            Files.write(dir.resolve(key), value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] get(String key) {
        try {
            Path file = dir.resolve(key);
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Path file = dir.resolve(key);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

