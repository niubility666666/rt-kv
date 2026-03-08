package org.rt.storage;

/**
 * 支持的存储后端类型。
 */
public enum StorageType {
    FILE,
    MYSQL,
    REDIS,
    LOCAL_DB,
    ELASTICSEARCH,
    MONGODB
}