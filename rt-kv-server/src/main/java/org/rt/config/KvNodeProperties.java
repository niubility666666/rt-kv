package org.rt.config;

import lombok.Data;
import org.rt.storage.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 单个 Raft 节点的配置模型。
 */
@Data
@ConfigurationProperties(prefix = "kv")
public class KvNodeProperties {

    /**
     * 当前节点 ID，例如 n1。
     */
    private String nodeId;

    /**
     * 节点本地数据目录。
     */
    private String dataDir;

    /**
     * Raft 集群节点列表。
     */
    private List<Peer> peers;

    /**
     * 存储后端配置。
     */
    private Storage storage = new Storage();

    /**
     * 集群节点配置项。
     */
    @Data
    public static class Peer {

        /**
         * 节点 ID，例如 n2。
         */
        private String id;

        /**
         * 节点地址，例如 localhost:8082。
         */
        private String address;
    }

    /**
     * 存储后端总配置。
     */
    @Data
    public static class Storage {

        /**
         * 存储类型，默认 FILE。
         */
        private StorageType type = StorageType.FILE;

        /**
         * 本地文件存储配置。
         */
        private File file = new File();

        /**
         * MySQL 存储配置。
         */
        private Mysql mysql = new Mysql();

        /**
         * Redis 存储配置。
         */
        private Redis redis = new Redis();

        /**
         * 本地 DB（H2）存储配置。
         */
        private LocalDb localDb = new LocalDb();

        /**
         * Elasticsearch 存储配置。
         */
        private Elasticsearch elasticsearch = new Elasticsearch();

        /**
         * MongoDB 存储配置。
         */
        private Mongodb mongodb = new Mongodb();
    }

    /**
     * 本地文件存储配置。
     */
    @Data
    public static class File {

        /**
         * 文件存储目录；为空时使用 kv.data-dir/nodeId/kv-file。
         */
        private String dir;
    }

    /**
     * MySQL 存储配置。
     */
    @Data
    public static class Mysql {

        /**
         * JDBC URL，例如 jdbc:mysql://127.0.0.1:3306/rt_kv。
         */
        private String url;

        /**
         * 用户名。
         */
        private String username;

        /**
         * 密码。
         */
        private String password;

        /**
         * 存储表名。
         */
        private String table = "rt_kv_store";
    }

    /**
     * Redis 存储配置。
     */
    @Data
    public static class Redis {

        /**
         * Redis 主机地址。
         */
        private String host = "127.0.0.1";

        /**
         * Redis 端口。
         */
        private int port = 6379;

        /**
         * Redis 密码，可为空。
         */
        private String password;

        /**
         * Redis DB 索引。
         */
        private int database = 0;

        /**
         * Redis Key 前缀。
         */
        private String keyPrefix = "rt-kv:";
    }

    /**
     * 本地 DB（H2）存储配置。
     */
    @Data
    public static class LocalDb {

        /**
         * JDBC URL；为空时自动拼接到 kv.data-dir 下。
         */
        private String url;

        /**
         * 用户名。
         */
        private String username = "sa";

        /**
         * 密码。
         */
        private String password = "";

        /**
         * 存储表名。
         */
        private String table = "rt_kv_store";
    }

    /**
     * Elasticsearch 存储配置。
     */
    @Data
    public static class Elasticsearch {

        /**
         * Elasticsearch HTTP 地址，例如 http://127.0.0.1:9200。
         */
        private String endpoint = "http://127.0.0.1:9200";

        /**
         * 基础认证用户名，可为空。
         */
        private String username;

        /**
         * 基础认证密码，可为空。
         */
        private String password;

        /**
         * 索引名。
         */
        private String index = "rt_kv_store";
    }

    /**
     * MongoDB 存储配置。
     */
    @Data
    public static class Mongodb {

        /**
         * MongoDB 连接串。
         */
        private String uri = "mongodb://127.0.0.1:27017";

        /**
         * 数据库名。
         */
        private String database = "rt_kv";

        /**
         * 集合名。
         */
        private String collection = "kv_store";
    }
}