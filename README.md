# rt-kv

`rt-kv` 是一个基于 Apache Ratis（Raft 协议）的轻量级分布式 Key-Value 存储系统，使用 Java + Maven 多模块构建。

## 项目定位

- 分布式系统 / Raft 协议学习项目
- 简化版 KV 存储原型
- 中小规模配置、元数据存储场景

## 核心特性

- 基于 Raft 共识协议（Apache Ratis）
- 支持多节点一致性复制
- 可插拔 KV 状态机（StateMachine）
- 可切换存储后端（MySQL / Redis / 本地 DB(H2) / 本地文件 / Elasticsearch / MongoDB）
- 清晰的 Client / Server / App 分层
- 统一命令协议层，客户端与服务端共享

## 模块结构

```text
rt-kv
├── rt-kv-common    # 协议与共享常量
├── rt-kv-server    # Raft 节点服务
├── rt-kv-client    # Java 客户端 SDK
└── rt-kv-app       # HTTP 网关示例应用
```

## 构建

```bash
mvn clean package -DskipTests
```

## 启动示例

启动 server：

```bash
java -jar rt-kv-server/target/rt-kv-server.jar
```

启动 app：

```bash
java -jar rt-kv-app/target/rt-kv-app.jar
```

## 存储后端配置

在 `rt-kv-server/src/main/resources/application.yml` 中通过 `kv.storage.type` 选择：

- `file`
- `mysql`
- `redis`
- `local_db`
- `elasticsearch`
- `mongodb`

示例：

```yaml
kv:
  storage:
    type: mysql
    mysql:
      url: jdbc:mysql://127.0.0.1:3306/rt_kv?useSSL=false&serverTimezone=UTC
      username: root
      password: root
      table: rt_kv_store
```

```yaml
kv:
  storage:
    type: redis
    redis:
      host: 127.0.0.1
      port: 6379
      password:
      database: 0
      key-prefix: rt-kv:
```

```yaml
kv:
  storage:
    type: local_db
    local-db:
      url: jdbc:h2:file:./data/n1/kv-localdb;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE
      username: sa
      password:
      table: rt_kv_store
```

```yaml
kv:
  storage:
    type: file
    file:
      dir: ./data/n1/kv-file
```

```yaml
kv:
  storage:
    type: elasticsearch
    elasticsearch:
      endpoint: http://127.0.0.1:9200
      username:
      password:
      index: rt_kv_store
```

```yaml
kv:
  storage:
    type: mongodb
    mongodb:
      uri: mongodb://127.0.0.1:27017
      database: rt_kv
      collection: kv_store
```

## HTTP API（rt-kv-app）

- 写入：`POST /kv/{key}`，Body 为 value 文本
- 读取：`GET /kv/{key}`
- 删除：`DELETE /kv/{key}`

示例：

```bash
curl -X POST "http://localhost:8080/kv/user:1" -d "Tom"
curl "http://localhost:8080/kv/user:1"
curl -X DELETE "http://localhost:8080/kv/user:1"
```

## License

MIT