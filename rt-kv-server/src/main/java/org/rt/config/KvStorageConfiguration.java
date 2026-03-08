package org.rt.config;

import org.rt.storage.ElasticsearchKvStorage;
import org.rt.storage.FileLogStorage;
import org.rt.storage.JdbcKvStorage;
import org.rt.storage.KvStorage;
import org.rt.storage.MongoKvStorage;
import org.rt.storage.RedisKvStorage;
import org.rt.storage.StorageType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * KV 存储后端装配配置。
 */
@Configuration
public class KvStorageConfiguration {

    /**
     * 根据配置创建具体存储实现。
     *
     * @param props 节点配置
     * @return 存储实现
     */
    @Bean
    public KvStorage kvStorage(KvNodeProperties props) {
        StorageType type = props.getStorage().getType();
        return switch (type) {
            case FILE -> buildFileStorage(props);
            case MYSQL -> buildMysqlStorage(props);
            case REDIS -> buildRedisStorage(props);
            case LOCAL_DB -> buildLocalDbStorage(props);
            case ELASTICSEARCH -> buildElasticsearchStorage(props);
            case MONGODB -> buildMongodbStorage(props);
        };
    }

    /**
     * 创建本地文件存储。
     *
     * @param props 节点配置
     * @return 文件存储实现
     */
    private KvStorage buildFileStorage(KvNodeProperties props) {
        String fileDir = props.getStorage().getFile().getDir();
        Path path = hasText(fileDir)
                ? Paths.get(fileDir)
                : Paths.get(props.getDataDir(), props.getNodeId(), "kv-file");

        try {
            return new FileLogStorage(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize file storage", e);
        }
    }

    /**
     * 创建 MySQL 存储。
     *
     * @param props 节点配置
     * @return JDBC 存储实现
     */
    private KvStorage buildMysqlStorage(KvNodeProperties props) {
        KvNodeProperties.Mysql mysql = props.getStorage().getMysql();
        if (!hasText(mysql.getUrl())) {
            throw new IllegalArgumentException("kv.storage.mysql.url must not be blank");
        }
        return new JdbcKvStorage(
                mysql.getUrl(),
                mysql.getUsername(),
                mysql.getPassword(),
                mysql.getTable()
        );
    }

    /**
     * 创建 Redis 存储。
     *
     * @param props 节点配置
     * @return Redis 存储实现
     */
    private KvStorage buildRedisStorage(KvNodeProperties props) {
        KvNodeProperties.Redis redis = props.getStorage().getRedis();
        return new RedisKvStorage(
                redis.getHost(),
                redis.getPort(),
                redis.getPassword(),
                redis.getDatabase(),
                redis.getKeyPrefix()
        );
    }

    /**
     * 创建本地 DB（H2）存储。
     *
     * @param props 节点配置
     * @return JDBC 存储实现
     */
    private KvStorage buildLocalDbStorage(KvNodeProperties props) {
        KvNodeProperties.LocalDb localDb = props.getStorage().getLocalDb();
        String jdbcUrl = hasText(localDb.getUrl())
                ? localDb.getUrl()
                : buildDefaultLocalDbUrl(props);

        return new JdbcKvStorage(
                jdbcUrl,
                localDb.getUsername(),
                localDb.getPassword(),
                localDb.getTable()
        );
    }

    /**
     * 创建 Elasticsearch 存储。
     *
     * @param props 节点配置
     * @return Elasticsearch 存储实现
     */
    private KvStorage buildElasticsearchStorage(KvNodeProperties props) {
        KvNodeProperties.Elasticsearch elasticsearch = props.getStorage().getElasticsearch();
        return new ElasticsearchKvStorage(
                elasticsearch.getEndpoint(),
                elasticsearch.getUsername(),
                elasticsearch.getPassword(),
                elasticsearch.getIndex()
        );
    }

    /**
     * 创建 MongoDB 存储。
     *
     * @param props 节点配置
     * @return MongoDB 存储实现
     */
    private KvStorage buildMongodbStorage(KvNodeProperties props) {
        KvNodeProperties.Mongodb mongodb = props.getStorage().getMongodb();
        return new MongoKvStorage(
                mongodb.getUri(),
                mongodb.getDatabase(),
                mongodb.getCollection()
        );
    }

    /**
     * 生成本地 H2 数据库默认 JDBC URL。
     *
     * @param props 节点配置
     * @return 默认 JDBC URL
     */
    private String buildDefaultLocalDbUrl(KvNodeProperties props) {
        Path dbPath = Paths.get(props.getDataDir(), props.getNodeId(), "kv-localdb").toAbsolutePath();
        String normalized = dbPath.toString().replace("\\", "/");
        return "jdbc:h2:file:" + normalized + ";MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE";
    }

    /**
     * 判断字符串是否有有效内容。
     *
     * @param value 待校验字符串
     * @return 非空且非空白时返回 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}