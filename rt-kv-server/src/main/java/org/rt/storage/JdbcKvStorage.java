package org.rt.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 基于 JDBC 的键值存储实现，可用于 MySQL 与本地 DB。
 */
public class JdbcKvStorage implements KvStorage {

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;

    /**
     * 创建 JDBC 存储实例。
     *
     * @param jdbcUrl JDBC 连接地址
     * @param username 用户名
     * @param password 密码
     * @param tableName 表名
     */
    public JdbcKvStorage(String jdbcUrl, String username, String password, String tableName) {
        this.jdbcUrl = requireText(jdbcUrl, "jdbcUrl");
        this.username = username;
        this.password = password;
        this.tableName = validateTableName(tableName);
        initSchema();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, byte[] value) {
        requireText(key, "key");
        Objects.requireNonNull(value, "value must not be null");

        String updateSql = "UPDATE " + tableName + " SET v = ? WHERE k = ?";
        String insertSql = "INSERT INTO " + tableName + " (k, v) VALUES (?, ?)";

        try (Connection connection = openConnection()) {
            int updated = executeUpdate(connection, updateSql, value, key);
            if (updated > 0) {
                return;
            }

            try {
                executeInsert(connection, insertSql, key, value);
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    executeUpdate(connection, updateSql, value, key);
                    return;
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("JDBC put failed for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] get(String key) {
        requireText(key, "key");

        String sql = "SELECT v FROM " + tableName + " WHERE k = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getBytes(1) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("JDBC get failed for key: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String key) {
        requireText(key, "key");

        String sql = "DELETE FROM " + tableName + " WHERE k = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("JDBC delete failed for key: " + key, e);
        }
    }

    /**
     * 初始化表结构。
     */
    private void initSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "k VARCHAR(512) PRIMARY KEY, "
                + "v BLOB NOT NULL"
                + ")";

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize JDBC storage schema", e);
        }
    }

    /**
     * 打开 JDBC 连接。
     *
     * @return JDBC 连接
     * @throws SQLException 连接失败时抛出
     */
    private Connection openConnection() throws SQLException {
        if (username == null || username.isBlank()) {
            return java.sql.DriverManager.getConnection(jdbcUrl);
        }
        return java.sql.DriverManager.getConnection(jdbcUrl, username, password == null ? "" : password);
    }

    /**
     * 执行更新语句。
     *
     * @param connection JDBC 连接
     * @param sql SQL 语句
     * @param value 值字节
     * @param key 键名
     * @return 影响行数
     * @throws SQLException SQL 执行失败时抛出
     */
    private int executeUpdate(Connection connection, String sql, byte[] value, String key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, value);
            statement.setString(2, key);
            return statement.executeUpdate();
        }
    }

    /**
     * 执行插入语句。
     *
     * @param connection JDBC 连接
     * @param sql SQL 语句
     * @param key 键名
     * @param value 值字节
     * @throws SQLException SQL 执行失败时抛出
     */
    private void executeInsert(Connection connection, String sql, String key, byte[] value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setBytes(2, value);
            statement.executeUpdate();
        }
    }

    /**
     * 判断异常是否为重复主键。
     *
     * @param exception SQL 异常
     * @return 是重复主键时返回 true
     */
    private boolean isDuplicateKey(SQLException exception) {
        if (exception instanceof SQLIntegrityConstraintViolationException) {
            return true;
        }
        String sqlState = exception.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
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

    /**
     * 校验表名是否合法。
     *
     * @param rawTableName 原始表名
     * @return 合法表名
     */
    private String validateTableName(String rawTableName) {
        String table = requireText(rawTableName, "tableName");
        if (!TABLE_NAME_PATTERN.matcher(table).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + rawTableName);
        }
        return table;
    }
}