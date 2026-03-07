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
- 本地文件持久化存储
- 清晰的 Client / Server / App 分层
- 新增 Common 协议层，统一命令格式与 GroupId

## 模块结构

```text
rt-kv
├── rt-kv-common    # 协议与共享常量
├── rt-kv-server    # Raft 节点服务
├── rt-kv-client    # Java 客户端 SDK
└── rt-kv-app       # HTTP 网关示例应用
```

## 关键优化（本次）

- 修复 client/server `RaftGroupId` 不一致问题
- 修复 `DELETE` 使用只读请求的问题（改为写请求）
- 抽离统一命令协议（`KvCommand` + `KvCommandCodec`）
- StateMachine 区分写路径（`applyTransaction`）与读路径（`query`）
- 文件存储 key 改为 Base64 文件名，避免非法字符与路径风险
- 增加 service 层，controller 只负责 HTTP 映射
- 补齐核心类和方法 Javadoc 注释

## 构建

```bash
mvn clean test
mvn clean package -DskipTests
```

## 启动示例

启动 3 个 server 节点（分别修改 `kv.node-id` 与 `kv.data-dir`）：

```bash
java -jar rt-kv-server/target/rt-kv-server.jar --spring.config.location=classpath:/application.yml
```

启动 app：

```bash
java -jar rt-kv-app/target/rt-kv-app.jar
```

## HTTP API（rt-kv-app）

- 写入: `POST /kv/{key}`，Body 为 value 文本
- 读取: `GET /kv/{key}`
- 删除: `DELETE /kv/{key}`

示例：

```bash
curl -X POST "http://localhost:8080/kv/user:1" -d "Tom"
curl "http://localhost:8080/kv/user:1"
curl -X DELETE "http://localhost:8080/kv/user:1"
```

## Roadmap

- Snapshot 支持
- Watch 机制
- gRPC API
- RocksDB 存储后端
- 集群动态扩缩容

## License

MIT
