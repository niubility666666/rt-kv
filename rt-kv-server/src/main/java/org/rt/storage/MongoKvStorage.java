package org.rt.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.DisposableBean;

import java.util.Base64;
import java.util.Objects;

/**
 * 基于 MongoDB 的键值存储实现。
 */
public class MongoKvStorage implements KvStorage, DisposableBean {

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    /**
     * 创建 MongoDB 存储实例。
     *
     * @param uri MongoDB 连接串
     * @param database 数据库名
     * @param collectionName 集合名
     */
    public MongoKvStorage(String uri, String database, String collectionName) {
        this.mongoClient = MongoClients.create(requireText(uri, "uri"));
        this.collection = mongoClient
                .getDatabase(requireText(database, "database"))
                .getCollection(requireText(collectionName, "collectionName"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, byte[] value) {
        requireText(key, "key");
        Objects.requireNonNull(value, "value must not be null");

        Document document = new Document("_id", key)
                .append("v", new Binary(value));

        collection.replaceOne(
                Filters.eq("_id", key),
                document,
                new ReplaceOptions().upsert(true)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(String key) {
        requireText(key, "key");

        Document document = collection.find(Filters.eq("_id", key)).first();
        if (document == null) {
            return null;
        }

        Object raw = document.get("v");
        if (raw instanceof Binary binary) {
            return binary.getData();
        }
        if (raw instanceof byte[] bytes) {
            return bytes;
        }
        if (raw instanceof String encoded) {
            return Base64.getDecoder().decode(encoded);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String key) {
        requireText(key, "key");
        return collection.deleteOne(Filters.eq("_id", key)).getDeletedCount() > 0;
    }

    /**
     * 关闭 MongoDB 客户端。
     */
    @Override
    public void destroy() {
        mongoClient.close();
    }

    /**
     * 校验并返回非空文本。
     *
     * @param value 文本值
     * @param field 字段名
     * @return 非空文本
     */
    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}