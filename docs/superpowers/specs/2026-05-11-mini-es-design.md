# mini-ES 设计规范

> 目标：忠实还原 Elasticsearch 核心架构，用于学习和作品集展示。
> 技术栈：Java 8 · Maven 多模块 · JUnit 4 · 纯 java.nio（无 Netty/Spring）

---

## 1. 项目定位

mini-ES 是 Elasticsearch 的教学级简化实现，覆盖以下核心子系统：

| 子系统 | 对齐真实 ES | mini-ES 实现策略 |
|--------|------------|----------------|
| 传输协议 | TCP 自定义二进制协议 | java.nio + 对齐 ES 消息头格式 |
| REST API | Netty HTTP | java.com.sun.net.httpserver（JDK 内置） |
| 存储引擎 | Lucene + Translog | 内存倒排索引 + 对齐 Translog 格式 |
| Action 框架 | Action/TransportAction | 轻量 ActionRegistry + handler |
| 集群状态 | ClusterState/RoutingTable | 单节点 ClusterState |
| 序列化 | StreamInput/StreamOutput | 仿照 ES Writeable 接口 |

**不实现**：ZenDiscovery、副本同步、Lucene 底层（用内存倒排索引代替）、Snapshot/Restore。

---

## 2. 多模块结构

```
mini-es/
├── pom.xml                          # parent pom，Java 8，管理所有子模块
├── mini-es-common/                  # 公共数据结构、序列化、工具
├── mini-es-server/                  # 核心引擎（索引、存储、集群、传输、REST）
├── mini-es-client/                  # Java 客户端（Transport + REST）
├── mini-es-examples/                # 示例应用
└── docs/                            # 设计文档
```

### 2.1 mini-es-common 包结构

```
org.miniEs.common
├── bytes/
│   ├── BytesReference           # 仿 ES BytesReference（不可变字节视图）
│   └── BytesArray               # 默认实现
├── io/
│   ├── StreamInput              # 仿 ES StreamInput（反序列化）
│   └── StreamOutput             # 仿 ES StreamOutput（序列化）
├── xcontent/
│   ├── XContentType             # JSON / SMILE（mini-ES 只实现 JSON）
│   ├── XContentBuilder          # JSON 构建器
│   └── XContentParser           # JSON 解析器（包装 Jackson）
├── settings/
│   └── Settings                 # 键值配置，仿 ES Settings.builder() 风格
├── unit/
│   └── TimeValue                # 时间值工具（ms/s/m/h）
└── collect/
    └── Tuple                    # 二元组
```

### 2.2 mini-es-server 包结构

```
org.miniEs.server
├── action/
│   ├── Action                   # 抽象 Action（名称 + Response reader）
│   ├── ActionRequest            # 抽象请求
│   ├── ActionResponse           # 抽象响应
│   ├── ActionListener           # 回调接口
│   ├── ActionRegistry           # 注册 action name → TransportAction
│   ├── index/
│   │   ├── IndexAction          # "indices:data/write/index"
│   │   ├── IndexRequest
│   │   ├── IndexResponse
│   │   └── TransportIndexAction
│   ├── get/
│   │   ├── GetAction            # "indices:data/read/get"
│   │   ├── GetRequest
│   │   ├── GetResponse
│   │   └── TransportGetAction
│   ├── delete/
│   │   ├── DeleteAction
│   │   ├── DeleteRequest
│   │   ├── DeleteResponse
│   │   └── TransportDeleteAction
│   ├── search/
│   │   ├── SearchAction         # "indices:data/read/search"
│   │   ├── SearchRequest
│   │   ├── SearchResponse
│   │   └── TransportSearchAction
│   ├── admin/indices/
│   │   ├── CreateIndexAction    # "indices:admin/create"
│   │   ├── DeleteIndexAction    # "indices:admin/delete"
│   │   └── ...
│   └── bulk/
│       ├── BulkAction           # "indices:data/write/bulk"
│       └── ...
├── cluster/
│   ├── ClusterState             # 集群快照
│   ├── ClusterStateVersion      # 版本号
│   ├── metadata/
│   │   ├── Metadata             # 所有索引元数据
│   │   ├── IndexMetadata        # 单个索引元数据
│   │   └── MappingMetadata      # 字段映射
│   └── routing/
│       ├── RoutingTable         # 路由表（index → shards）
│       ├── IndexRoutingTable
│       └── ShardRoutingState    # UNASSIGNED/INITIALIZING/STARTED/RELOCATING
├── engine/
│   ├── Engine                   # 抽象引擎（index/delete/get/searcher）
│   ├── EngineConfig
│   ├── InternalEngine           # 主实现：内存倒排索引
│   ├── InvertedIndex            # 倒排索引实现
│   ├── PostingList              # 倒排列表（docId → term frequency）
│   └── Searcher                 # 搜索快照
├── index/
│   ├── IndexService             # 单索引生命周期
│   ├── IndexSettings            # 分片数、副本数等
│   └── shard/
│       ├── IndexShard           # 分片核心
│       ├── ShardId              # (index, shardNumber)
│       └── ShardPath            # 数据目录路径
├── store/
│   ├── Store                    # 存储层抽象
│   └── StoreStats               # 统计
├── translog/
│   ├── Translog                 # 事务日志主类
│   ├── TranslogHeader           # 文件头（魔数、版本、主 term、generation）
│   ├── TranslogWriter           # 追加写入（FileChannel + NIO）
│   ├── TranslogReader           # 顺序读取
│   ├── TranslogSnapshot         # 操作快照迭代器
│   └── Translog.Operation       # INDEX / DELETE / NO_OP
├── transport/
│   ├── TransportService         # NIO Selector 主循环
│   ├── TransportMessage         # 传输消息（header + body）
│   ├── TransportRequest         # 传输层请求
│   ├── TransportResponse        # 传输层响应
│   ├── TransportRequestHandler  # 处理器接口
│   └── TcpChannel               # 每个连接的读写缓冲
├── http/
│   ├── HttpServer               # 基于 com.sun.net.httpserver
│   ├── RestController           # 路由注册
│   ├── RestRequest
│   ├── RestResponse
│   ├── RestStatus               # HTTP 状态码枚举
│   └── action/
│       ├── RestIndexAction
│       ├── RestGetAction
│       ├── RestDeleteAction
│       ├── RestSearchAction
│       ├── RestCreateIndexAction
│       ├── RestDeleteIndexAction
│       └── RestBulkAction
├── indices/
│   └── IndicesService           # 管理所有 IndexService（Map<name, IndexService>）
├── node/
│   └── Node                     # 服务入口，组装所有子系统
└── ElasticsearchServer          # main() 入口
```

### 2.3 mini-es-client 包结构

```
org.miniEs.client
├── transport/
│   ├── TransportClient          # 仿 ES TransportClient（TCP 连接）
│   └── TransportClientConfig
└── rest/
    ├── RestHighLevelClient      # 仿 ES RestHighLevelClient（HTTP 连接）
    ├── RestClient               # 底层 HTTP 连接
    └── RestClientBuilder
```

### 2.4 mini-es-examples 包结构

```
org.miniEs.examples
├── GettingStartedExample        # 基本 CRUD
├── SearchExample                # 全文搜索
├── BulkExample                  # 批量写入
└── EmbeddedNodeExample          # 嵌入式服务端测试
```

---

## 3. 传输层协议（对齐 ES 真实格式）

### 3.1 消息头格式（18 字节）

```
Offset  Size  Type    Field
0-1     2     byte[]  Magic: 'ES' (0x45, 0x53)
2-3     2     short   Version (1 = mini-ES v1)
4-11    8     long    RequestId (单调递增，匹配响应)
12      1     byte    Status Flags
                        bit0: isResponse (0=Request, 1=Response)
                        bit1: isError    (0=正常, 1=异常)
                        bit2: isCompress (0=无压缩, mini-ES 永远为 0)
13-16   4     int     Message Length (后续字节数)
```

### 3.2 消息体格式

**请求（isResponse=0）：**
```
[UTF-8 String] action name          (例: "indices:data/write/index")
[Writeable]    request object       (IndexRequest 序列化内容)
```

**响应（isResponse=1，isError=0）：**
```
[Writeable]    response object      (IndexResponse 序列化内容)
```

**错误响应（isResponse=1，isError=1）：**
```
[UTF-8 String] exception class name
[UTF-8 String] exception message
```

### 3.3 NIO 传输实现

- 单 Selector 线程：Accept + Read（与 ES 简化）
- 独立线程池：执行 Action（防止阻塞 Selector）
- 读缓冲：每个连接 64KB ByteBuffer，不够则扩容（参考 ES TcpTransport 的 headerBytesToRead 逻辑）
- 写缓冲：LinkedBlockingQueue<ByteBuffer>，WRITE 事件触发发送

---

## 4. 存储设计（对齐 ES 文件命名）

### 4.1 目录结构

```
data/
└── nodes/
    └── 0/                        # 节点 ID（mini-ES 固定为 0）
        └── indices/
            └── {indexUUID}/      # 索引 UUID（随机生成，对齐 ES）
                └── 0/            # 分片编号
                    ├── index/    # 索引数据（mini-ES 内存，仅元数据持久化）
                    │   └── index.meta  # 映射、设置等
                    └── translog/ # 事务日志
                        ├── translog-1.tlog   # 日志文件
                        └── translog-1.ckp    # 检查点
```

### 4.2 Translog 文件格式（精确对齐 ES）

**TranslogHeader（文件头，固定 26 字节）：**
```
Offset  Size  Type    Field
0-3     4     int     Magic: 0x4C4A2054 ('LJT ')  (mini-ES 使用相同魔数)
4-5     2     short   Version: 2
6-13    8     long    Primary Term
14-21   8     long    Generation (文件序号，从 1 开始)
22-25   4     int     Header CRC
```

**Translog Operation（每条记录）：**
```
Offset  Size  Type    Field
0-3     4     int     Checksum (CRC32 of subsequent bytes)
4-7     4     int     Length (operation bytes 长度)
8       1     byte    OperationType: 0=INDEX, 1=DELETE, 2=NO_OP
9-16    8     long    Primary Term
17-24   8     long    Sequence No

--- OperationType=INDEX ---
25+     var   String  index name
var     var   String  type ("_doc")
var     var   String  id
var     8     long    version
var     var   bytes   source (JSON body)

--- OperationType=DELETE ---
25+     var   String  index name
var     var   String  type
var     var   String  id
var     8     long    version
```

**Checkpoint 文件（.ckp，固定 48 字节）：**
```
Offset  Size  Type    Field
0-7     8     long    offset (当前写入偏移)
8-11    4     int     numOps (已写操作数)
12-19   8     long    generation
20-27   8     long    minSeqNo
28-35   8     long    maxSeqNo
36-43   8     long    globalCheckpoint
44-47   4     int     trimmedAboveSeqNo reserved (mini-ES 设 0)
```

### 4.3 倒排索引（内存实现）

```java
// 对齐 Lucene 概念，但用内存数据结构实现
public class InvertedIndex {
    // term → docIds（PostingList）
    Map<String, TreeSet<Integer>> postings;
    // docId → source document
    Map<Integer, Map<String, Object>> documents;
    // docId → version
    Map<String, Long> versions;  // key = type + "_" + id
    // docId → seqNo
    Map<Integer, Long> seqNos;
    // 已删除的 docId 集合（soft delete，对齐 Lucene 设计）
    Set<Integer> deletedDocs;
    // 当前最大 docId（自增）
    AtomicInteger maxDoc;
}
```

---

## 5. Action 框架（对齐 ES Action 体系）

### 5.1 Action 命名规范（精确对齐 ES）

| Action | 名称 |
|--------|------|
| Index  | `indices:data/write/index` |
| Delete | `indices:data/write/delete` |
| Get    | `indices:data/read/get` |
| Search | `indices:data/read/search` |
| Bulk   | `indices:data/write/bulk` |
| CreateIndex | `indices:admin/create` |
| DeleteIndex | `indices:admin/delete` |
| ClusterHealth | `cluster:monitor/health` |

### 5.2 核心接口

```java
// 仿 ES Writeable
public interface Writeable {
    void writeTo(StreamOutput out) throws IOException;
    interface Reader<V> {
        V read(StreamInput in) throws IOException;
    }
}

// 仿 ES ActionRequest
public abstract class ActionRequest implements Writeable {
    public abstract ActionRequestValidationException validate();
}

// 仿 ES TransportAction（不用 DI 框架，手动注入）
public abstract class TransportAction<Request extends ActionRequest, Response extends ActionResponse> {
    protected final ActionRegistry registry;
    protected final IndicesService indicesService;

    public abstract void doExecute(Request request, ActionListener<Response> listener);
}
```

---

## 6. REST API 端点

| Method | Path | Action |
|--------|------|--------|
| PUT | `/{index}` | CreateIndex |
| DELETE | `/{index}` | DeleteIndex |
| GET | `/_cluster/health` | ClusterHealth |
| PUT/POST | `/{index}/_doc/{id}` | Index |
| GET | `/{index}/_doc/{id}` | Get |
| DELETE | `/{index}/_doc/{id}` | Delete |
| POST | `/{index}/_search` | Search |
| POST | `/_bulk` | Bulk |
| GET | `/{index}/_count` | Count |

---

## 7. 序列化设计（StreamInput/StreamOutput）

仿 ES Writeable 体系，基于 `java.io.DataInputStream` / `DataOutputStream` 包装。

| 方法 | 格式 |
|------|------|
| `writeString(s)` | 2 字节 UTF 长度 + UTF-8 字节 |
| `writeInt(n)` | 4 字节 big-endian |
| `writeLong(n)` | 8 字节 big-endian |
| `writeBoolean(b)` | 1 字节 |
| `writeBytesReference(b)` | 4 字节长度 + 字节数组 |
| `writeOptionalString(s)` | 1 字节标志 + writeString |
| `writeMap(m)` | 4 字节数量 + [key, value]* |

---

## 8. 错误体系（对齐 ES Exception 层级）

```
ElasticsearchException (RuntimeException)
├── IndexNotFoundException
├── DocumentNotFoundException  (对齐 ES DocumentMissingException)
├── IndexAlreadyExistsException (对齐 ES ResourceAlreadyExistsException)
├── VersionConflictException
├── SearchPhaseExecutionException
└── TransportException
    ├── ConnectTransportException
    └── SendRequestTransportException
```

---

## 9. 测试策略

| 层级 | 测试类型 | 覆盖重点 |
|------|----------|--------|
| StreamInput/Output | Unit | 所有 write/read 方法往返一致 |
| Translog | Unit | 写入→读取→重放，崩溃恢复 |
| InvertedIndex | Unit | index/delete/get/search 正确性 |
| IndexShard | Unit | 状态机（CREATED→RECOVERING→STARTED） |
| TransportMessage | Unit | 消息编解码，边界条件 |
| RestController | Integration | HTTP 路由 + JSON 响应 |
| EmbeddedNode | Integration | 端到端 CRUD + Search |

---

## 10. 与真实 ES 的差异说明

| 特性 | 真实 ES | mini-ES |
|------|---------|--------|
| 底层存储 | Apache Lucene | 内存倒排索引 |
| 网络框架 | Netty | java.nio Selector |
| HTTP 服务 | Netty HTTP | com.sun.net.httpserver |
| 集群发现 | ZenDiscovery | 单节点，无发现 |
| 副本同步 | 主从复制 | 不实现 |
| 查询 DSL | 完整 Query DSL | match/term/bool/range/match_all |
| 聚合 | 完整聚合框架 | terms/date_histogram/avg/max/min |
| 安全 | X-Pack Security | 不实现 |
| 快照 | Snapshot/Restore | 不实现 |
