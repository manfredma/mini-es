# Phase 1: mini-es-server 实现计划

## 目标
实现 mini-ES 服务端核心，包含：传输层（NIO）、Translog、倒排索引引擎、Action 框架、REST API、单节点运行入口。

## 任务清单

---

### P1-1: 传输层消息编解码
**对齐**：`org.elasticsearch.transport.TransportMessage` / `TcpTransport`

测试先行（RED）：
```java
public class TransportMessageCodecTest {
    @Test public void testEncodeRequest();       // 序列化请求消息
    @Test public void testDecodeRequest();       // 反序列化请求消息
    @Test public void testEncodeResponse();      // 序列化响应
    @Test public void testDecodeResponse();
    @Test public void testEncodeErrorResponse(); // 序列化异常响应
    @Test public void testMagicValidation();     // 非法魔数抛异常
    @Test public void testIncompleteHeader();    // 数据不足时返回 null
}
```

实现（GREEN）：
- `TransportMessage`：POJO（requestId, status, actionName, payload）
- `TransportMessageCodec`：`encode(StreamOutput)` / `decode(ByteBuffer)` → 可能返回 null（半包）
- `MessageHeader`：18 字节头部常量定义

---

### P1-2: NIO 传输服务
**对齐**：`org.elasticsearch.transport.nio.NioTransport`

测试先行（RED）：
```java
public class TransportServiceTest {
    @Test public void testStartStop();
    @Test public void testSendRequestAndReceiveResponse() throws Exception;
    @Test public void testSendToUnknownNode();  // 连接不存在节点抛异常
}
```

实现（GREEN）：
- `TransportService`：Selector 主循环（单线程），消息派发到线程池
- `TcpChannel`：读写缓冲 + 挂起写队列
- `TransportRequestHandler<T>`：接口（messageReceived）
- `NodeAddress`：host + port

---

### P1-3: Translog
**对齐**：`org.elasticsearch.index.translog.Translog`

测试先行（RED）：
```java
public class TranslogTest {
    @Test public void testWriteAndReadIndexOp();   // 写入 INDEX 操作并读取
    @Test public void testWriteAndReadDeleteOp();
    @Test public void testCheckpointPersisted();   // 写入后 .ckp 文件正确
    @Test public void testRecoverFromFile();       // 重启后从 .tlog 重放
    @Test public void testRollGeneration();        // rollGeneration() 创建新文件
    @Test public void testCRC();                   // CRC 损坏时抛异常
}
```

实现（GREEN）：
- `TranslogHeader`：writeHeader() / readHeader()，含魔数校验
- `TranslogWriter`：NIO FileChannel 追加写，flush 后刷盘
- `TranslogReader`：顺序读取、CRC 校验
- `TranslogSnapshot`：Iterator<Operation>
- `Translog`：生命周期管理（open/close/roll/recovery）
- `Translog.Index`、`Translog.Delete`、`Translog.NoOp` 实现 `Translog.Operation`

---

### P1-4: 倒排索引引擎（InternalEngine）
**对齐**：`org.elasticsearch.index.engine.InternalEngine`

测试先行（RED）：
```java
public class InternalEngineTest {
    @Test public void testIndexAndGet();
    @Test public void testIndexAndSearch();
    @Test public void testDelete();
    @Test public void testVersionConflict();  // 乐观锁版本冲突
    @Test public void testSoftDelete();       // 删除后 get 返回 null
    @Test public void testRefresh();          // refresh 后 search 可见
    @Test public void testFlush();            // flush 触发 translog 刷盘
}
```

实现（GREEN）：
- `InvertedIndex`：term → `TreeSet<Integer>` postings，docId → source，docId → version
- `InternalEngine`：包装 InvertedIndex + Translog，实现 index/delete/get/search
- `Engine.Index`、`Engine.Delete`、`Engine.Get`：操作 POJO
- `Engine.IndexResult`、`Engine.DeleteResult`：结果 POJO（含 version、seqNo、created/updated/deleted）
- `Searcher`：`TopDocs`-like 结果集合

**搜索支持（Query 类型）：**
- `MatchAllQuery`
- `TermQuery`（精确匹配一个词项）
- `MatchQuery`（分词后 OR 查询）
- `BoolQuery`（must/should/must_not）
- `RangeQuery`（数值范围）

---

### P1-5: IndexShard
**对齐**：`org.elasticsearch.index.shard.IndexShard`

测试先行（RED）：
```java
public class IndexShardTest {
    @Test public void testShardStateTransition();  // CREATED→RECOVERING→STARTED
    @Test public void testIndexDocument();
    @Test public void testGetDocument();
    @Test public void testSearchDocuments();
    @Test public void testDeleteDocument();
    @Test public void testRecoveryFromTranslog();  // 重启后从 translog 恢复
}
```

实现（GREEN）：
- `ShardId`：(index, shardNumber)
- `ShardRoutingState`：枚举 UNASSIGNED/INITIALIZING/STARTED/RELOCATING
- `IndexShard`：组合 Engine + Translog + Store，管理分片生命周期
- `IndexShard.State`：枚举（CREATED, RECOVERING, POST_RECOVERY, STARTED, CLOSING, CLOSED）

---

### P1-6: 集群状态（ClusterState）
**对齐**：`org.elasticsearch.cluster.ClusterState`

测试先行（RED）：
```java
public class ClusterStateTest {
    @Test public void testCreateIndex();
    @Test public void testDeleteIndex();
    @Test public void testRoutingTableContainsShard();
}
```

实现（GREEN）：
- `ClusterState`：不可变 snapshot（version + metadata + routingTable）
- `Metadata`：`Map<String, IndexMetadata>`
- `IndexMetadata`：name, uuid, settings, mappings, numberOfShards, numberOfReplicas
- `RoutingTable`：`Map<String, IndexRoutingTable>`
- `IndexRoutingTable`：`List<ShardRouting>`
- `ClusterStateApplier`：应用状态变更
- `IndicesService`：持有 `Map<String, IndexService>` + 当前 `ClusterState`

---

### P1-7: Action 框架
**对齐**：`org.elasticsearch.action`

测试先行（RED）：
```java
public class ActionRegistryTest {
    @Test public void testRegisterAndExecute();
    @Test public void testUnknownActionThrows();
}
public class TransportIndexActionTest {
    @Test public void testIndexNewDocument();
    @Test public void testIndexExistingDocument();
    @Test public void testIndexToNonExistentIndex();  // 抛 IndexNotFoundException
}
public class TransportGetActionTest {
    @Test public void testGetExistingDoc();
    @Test public void testGetNonExistentDoc();        // 返回 found=false
}
public class TransportSearchActionTest {
    @Test public void testMatchAll();
    @Test public void testTermQuery();
    @Test public void testBoolQuery();
}
public class TransportDeleteActionTest {
    @Test public void testDeleteExistingDoc();
    @Test public void testDeleteNonExistentDoc();
}
public class TransportBulkActionTest {
    @Test public void testBulkIndex();
    @Test public void testBulkMixed();               // index + delete 混合
}
```

实现（GREEN）：
- `Action<Req, Resp>`、`ActionRequest`、`ActionResponse`、`ActionListener<T>`
- `ActionRegistry`：action name → TransportAction
- `TransportIndexAction`、`TransportGetAction`、`TransportDeleteAction`
- `TransportSearchAction`、`TransportBulkAction`
- `TransportCreateIndexAction`、`TransportDeleteIndexAction`
- `TransportClusterHealthAction`

---

### P1-8: REST API 层
**对齐**：`org.elasticsearch.rest`

测试先行（RED）：
```java
public class RestControllerTest {
    @Test public void testRegisterAndRoute();
    @Test public void testNotFoundPath();
    @Test public void testMethodNotAllowed();
}
public class RestIndexActionTest {
    // 通过 EmbeddedServer 做集成测试
    @Test public void testPutDocument() throws Exception;
    @Test public void testPostDocument() throws Exception;  // 自动生成 id
}
public class RestGetActionTest {
    @Test public void testGetExistingDocument() throws Exception;
    @Test public void testGetMissingDocument() throws Exception;    // 404
}
public class RestSearchActionTest {
    @Test public void testSearchMatchAll() throws Exception;
    @Test public void testSearchTermQuery() throws Exception;
}
public class RestCreateIndexTest {
    @Test public void testCreateIndex() throws Exception;
    @Test public void testCreateDuplicateIndex() throws Exception;  // 400
}
```

实现（GREEN）：
- `RestRequest`：method, path, params, content
- `RestResponse`：status, contentType, content
- `RestStatus`：枚举（OK_200, CREATED_201, NOT_FOUND_404 等）
- `RestController`：TrieRouter（前缀树路由，支持 `{index}` 占位符）
- `HttpServer`：包装 `com.sun.net.httpserver.HttpServer`
- `RestHandler`：接口，`routes()` + `handleRequest()`
- 各 `RestXxxAction` 实现

---

### P1-9: Node（服务器启动入口）
**对齐**：`org.elasticsearch.node.Node`

测试先行（RED）：
```java
public class NodeIntegrationTest {
    @Test public void testStartAndStop() throws Exception;
    @Test public void testFullCrudCycle() throws Exception;  // 端到端：建索引→写→读→搜索→删
    @Test public void testTranslogRecovery() throws Exception;  // 停止→重启→数据仍在
}
```

实现（GREEN）：
- `Node`：组装所有子系统，start()/stop() 生命周期
- `ElasticsearchServer`：main() 解析参数，启动 Node
- `NodeConfig`：port, dataDir, clusterName, nodeName

---

## Commit 计划
- `feat(server): add transport message codec`
- `feat(server): add NIO transport service`
- `feat(server): add translog with NIO file channel`
- `feat(server): add inverted index engine`
- `feat(server): add index shard with state machine`
- `feat(server): add cluster state and indices service`
- `feat(server): add action framework and transport actions`
- `feat(server): add REST API layer`
- `feat(server): add Node startup entry point`
