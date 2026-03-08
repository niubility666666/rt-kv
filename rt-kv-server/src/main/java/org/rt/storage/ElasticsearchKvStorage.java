package org.rt.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 Elasticsearch HTTP API 的键值存储实现。
 */
public class ElasticsearchKvStorage implements KvStorage {

    private static final Pattern VALUE_PATTERN = Pattern.compile("\"v\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern INDEX_PATTERN = Pattern.compile("^[a-z0-9._-]+$");

    private static final Base64.Encoder VALUE_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder VALUE_DECODER = Base64.getDecoder();
    private static final Base64.Encoder ID_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final HttpClient httpClient;
    private final String endpoint;
    private final String index;
    private final String authorization;

    /**
     * 创建 Elasticsearch 存储实例。
     *
     * @param endpoint Elasticsearch HTTP 地址
     * @param username 基础认证用户名
     * @param password 基础认证密码
     * @param index 索引名
     */
    public ElasticsearchKvStorage(String endpoint, String username, String password, String index) {
        this.endpoint = normalizeEndpoint(requireText(endpoint, "endpoint"));
        this.index = validateIndex(index);
        this.authorization = buildAuthorization(username, password);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ensureIndexExists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, byte[] value) {
        requireText(key, "key");
        Objects.requireNonNull(value, "value must not be null");

        String payload = "{\"k\":\"" + escapeJson(key) + "\",\"v\":\""
                + VALUE_ENCODER.encodeToString(value)
                + "\"}";

        HttpResponse<String> response = sendRaw(
                "PUT",
                "/" + index + "/_doc/" + documentId(key) + "?refresh=true",
                payload
        );

        int statusCode = response.statusCode();
        if (statusCode != 200 && statusCode != 201) {
            throw new IllegalStateException("Elasticsearch put failed, status=" + statusCode + ", body=" + response.body());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(String key) {
        requireText(key, "key");

        HttpResponse<String> response = sendRaw(
                "GET",
                "/" + index + "/_doc/" + documentId(key),
                null
        );

        int statusCode = response.statusCode();
        if (statusCode == 404) {
            return null;
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("Elasticsearch get failed, status=" + statusCode + ", body=" + response.body());
        }

        String body = response.body();
        if (body.contains("\"found\":false")) {
            return null;
        }

        Matcher matcher = VALUE_PATTERN.matcher(body);
        if (!matcher.find()) {
            return null;
        }

        try {
            return VALUE_DECODER.decode(matcher.group(1));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Elasticsearch value decode failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String key) {
        requireText(key, "key");

        HttpResponse<String> response = sendRaw(
                "DELETE",
                "/" + index + "/_doc/" + documentId(key) + "?refresh=true",
                null
        );

        int statusCode = response.statusCode();
        if (statusCode == 404) {
            return false;
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("Elasticsearch delete failed, status=" + statusCode + ", body=" + response.body());
        }

        return !response.body().contains("\"result\":\"not_found\"");
    }

    /**
     * 确保索引存在。
     */
    private void ensureIndexExists() {
        HttpResponse<String> response = sendRaw("PUT", "/" + index, null);
        int statusCode = response.statusCode();

        if (statusCode == 200 || statusCode == 201) {
            return;
        }
        if (statusCode == 400 && response.body().contains("resource_already_exists_exception")) {
            return;
        }

        throw new IllegalStateException("Failed to initialize Elasticsearch index, status="
                + statusCode + ", body=" + response.body());
    }

    /**
     * 发送 HTTP 请求并返回响应。
     *
     * @param method HTTP 方法
     * @param path 路径与查询参数
     * @param body 请求体
     * @return HTTP 响应
     */
    private HttpResponse<String> sendRaw(String method, String path, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + path))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

        if (authorization != null) {
            builder.header("Authorization", authorization);
        }

        HttpRequest.BodyPublisher bodyPublisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);

        HttpRequest request = builder.method(method, bodyPublisher).build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Elasticsearch request interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch request failed", e);
        }
    }

    /**
     * 生成文档 ID。
     *
     * @param key 键名
     * @return 文档 ID
     */
    private String documentId(String key) {
        return ID_ENCODER.encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构建基础认证头。
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证头；未配置时返回 null
     */
    private String buildAuthorization(String username, String password) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String credential = username + ":" + (password == null ? "" : password);
        return "Basic " + Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准化 endpoint。
     *
     * @param rawEndpoint 原始 endpoint
     * @return 标准化 endpoint
     */
    private String normalizeEndpoint(String rawEndpoint) {
        String value = rawEndpoint.endsWith("/") ? rawEndpoint.substring(0, rawEndpoint.length() - 1) : rawEndpoint;
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw new IllegalArgumentException("endpoint must start with http:// or https://");
        }
        return value;
    }

    /**
     * 校验索引名。
     *
     * @param rawIndex 原始索引名
     * @return 合法索引名
     */
    private String validateIndex(String rawIndex) {
        String value = requireText(rawIndex, "index");
        if (!INDEX_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Elasticsearch index: " + rawIndex);
        }
        return value;
    }

    /**
     * 转义 JSON 字符串。
     *
     * @param value 原始字符串
     * @return 转义后字符串
     */
    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
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