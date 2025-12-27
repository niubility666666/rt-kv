# rt-kv

一个 **基于 Apache Ratis (Raft 协议) 实现的轻量级分布式 Key-Value 存储系统**，使用 Java + Maven 构建，适合作为：

* 分布式系统 / Raft 协议学习项目
* 简化版 KV 存储原型
* 中小规模配置、元数据存储

---

## ✨ 项目特性

* ✅ **基于 Raft 共识协议**（Apache Ratis）
* ✅ 支持 **多节点一致性复制**
* ✅ 可插拔的 **KV 状态机（StateMachine）**
* ✅ 本地文件持久化日志（Log Storage）
* ✅ 清晰的 Client / Server 架构
* ✅ 使用 Maven 构建，易于二次开发

---

## 🧱 项目结构

```text
rt-kv
├── pom.xml                    # Maven 配置
├── rt-kv-client               # KV 客户端模块
│   └── KvClient.java
├── rt-kv-server               # KV 服务端模块
│   ├── RaftServerBootstrap    # Raft Server 启动类
│   ├── machine/
│   │   └── KvStateMachine     # Raft 状态机实现
│   ├── storage/
│   │   ├── KvStorage          # KV 存储接口
│   │   └── FileLogStorage     # 基于文件的存储实现
│   └── config/
│       └── KvConfig           # 节点与 Raft 配置
└── resources/
    └── application.yml        # 服务端配置文件
```

---

## ⚙️ 技术栈

* **Java 17+**（推荐 JDK 21）
* **Apache Ratis**（Raft 协议实现）
* **Maven**
* **YAML** 配置

---

## 🚀 快速开始

### 1️⃣ 构建项目

```bash
mvn clean package -DskipTests
```

---

### 2️⃣ 启动 KV Server（示例：3 节点）

分别在不同终端或机器上启动：

```bash
java -jar rt-kv-server/target/rt-kv-server.jar \
  --nodeId n1 \
  --port 8081
```

```bash
java -jar rt-kv-server/target/rt-kv-server.jar \
  --nodeId n2 \
  --port 8082
```

```bash
java -jar rt-kv-server/target/rt-kv-server.jar \
  --nodeId n3 \
  --port 8083
```

> ⚠️ 节点信息需与 `application.yml` 中的 Raft Group 配置一致

---

### 3️⃣ 使用客户端读写数据

```java
KvClient client = new KvClient("localhost:8081");

client.put("name", "rt-kv");
String value = client.get("name");
System.out.println(value);
```

---

## 🧠 核心设计说明

### Raft 状态机（KvStateMachine）

* 所有 **PUT / DELETE** 操作通过 Raft Log 复制
* Leader 提交后由 StateMachine apply
* 保证多节点 KV 数据强一致

### 存储层设计

```java
interface KvStorage {
    void put(String key, String value);
    String get(String key);
}
```

当前实现：

* `FileLogStorage`：基于本地文件持久化

可扩展为：

* RocksDB
* LevelDB
* 内存 + Snapshot

---

## 📌 适合学习的知识点

* Raft 协议核心流程（Leader / Follower / Log Replication）
* Apache Ratis 使用方式
* 分布式一致性 KV 设计
* StateMachine + Log Storage 解耦
* 分布式系统启动与配置管理

---

## 🛣️ Roadmap

* [ ] 支持 Snapshot
* [ ] 支持 Watch / 监听机制
* [ ] HTTP / gRPC API
* [ ] RocksDB 存储引擎
* [ ] 集群动态扩缩容
* [ ] Spring Boot Starter 封装

---

## 🤝 贡献指南

欢迎 Issue / PR：

1. Fork 本仓库
2. 新建分支：`feature/xxx`
3. 提交代码
4. 发起 Pull Request

---

## 📄 License

MIT License

---

## 🙋 作者

* GitHub: **niubility666666**
* 项目目的：学习 & 分享分布式存储实现原理

如果这个项目对你有帮助，欢迎 ⭐ Star 支持！
