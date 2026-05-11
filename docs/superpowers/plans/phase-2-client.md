# Phase 2: mini-es-client 实现计划

## 目标
实现 mini-ES 客户端，提供两种连接方式：
1. **RestHighLevelClient**：HTTP 连接，面向应用开发（对齐 ES RestHighLevelClient API）
2. **TransportClient**：TCP 连接，直接通过传输层发送 Action（对齐 ES TransportClient）

---

## 任务清单

### P2-1: RestClient（底层 HTTP 连接）
**对齐**：`org.elasticsearch.client.RestClient`

测试先行（RED）：
```java
public class RestClientTest {
    // 使用 EmbeddedServer 做集成测试
    @Test public void testGet() throws Exception;
    @Test public void testPost() throws Exception;
    @Test public void testPut() throws Exception;
    @Test public void testDelete() throws Exception;
    @Test public void testConnectionRefused();     // 无法连接时抛异常
}
```

实现（GREEN）：
- `RestClient`：基于 `java.net.HttpURLConnection`（不依赖外部 HTTP 库）
- 支持：GET/POST/PUT/DELETE
- `Request`：method, endpoint, params, entity
- `Response`：statusLine, headers, entity
- `RestClientBuilder`：builder 模式

---

### P2-2: RestHighLevelClient
**对齐**：`org.elasticsearch.client.RestHighLevelClient`

测试先行（RED）：
```java
public class RestHighLevelClientTest {
    private static Node node;       // 嵌入式服务端
    private RestHighLevelClient client;

    @Test public void testIndex() throws Exception;
    @Test public void testGet() throws Exception;
    @Test public void testDelete() throws Exception;
    @Test public void testSearch_matchAll() throws Exception;
    @Test public void testSearch_termQuery() throws Exception;
    @Test public void testBulk() throws Exception;
    @Test public void testCreateIndex() throws Exception;
    @Test public void testDeleteIndex() throws Exception;
    @Test public void testClusterHealth() throws Exception;
}
```

实现（GREEN）：
- `RestHighLevelClient`：包装 `RestClient`，提供高级 API
- `IndexRequest` / `IndexResponse`（client 侧）
- `GetRequest` / `GetResponse`
- `DeleteRequest` / `DeleteResponse`
- `SearchRequest` / `SearchResponse`（含 `SearchHits`、`SearchHit`）
- `BulkRequest` / `BulkResponse`（含 `BulkItemResponse`）
- `CreateIndexRequest` / `CreateIndexResponse`
- `ClusterHealthRequest` / `ClusterHealthResponse`

**关键 API 签名（对齐 ES）：**
```java
// Index
IndexResponse response = client.index(
    new IndexRequest("myindex").id("1").source(Map.of("field", "value")),
    RequestOptions.DEFAULT
);

// Get
GetResponse response = client.get(
    new GetRequest("myindex", "1"),
    RequestOptions.DEFAULT
);

// Search
SearchResponse response = client.search(
    new SearchRequest("myindex")
        .source(new SearchSourceBuilder()
            .query(QueryBuilders.matchAllQuery())
            .from(0).size(10)),
    RequestOptions.DEFAULT
);
```

---

### P2-3: TransportClient（TCP 直连）
**对齐**：`org.elasticsearch.client.transport.TransportClient`

测试先行（RED）：
```java
public class TransportClientTest {
    @Test public void testConnectAndPing() throws Exception;
    @Test public void testIndexViaTcp() throws Exception;
    @Test public void testGetViaTcp() throws Exception;
    @Test public void testSearchViaTcp() throws Exception;
}
```

实现（GREEN）：
- `TransportClient`：复用 `TransportService`，发送 TransportRequest 并等待响应
- 基于 `CompletableFuture` 实现异步 → 同步转换
- `PreBuiltTransportClient`：快速创建实例的工厂方法

---

### P2-4: QueryBuilders / SearchSourceBuilder
**对齐**：`org.elasticsearch.index.query.QueryBuilders`

```java
public class QueryBuildersTest {
    @Test public void testMatchAllQuery_toJson();
    @Test public void testTermQuery_toJson();
    @Test public void testMatchQuery_toJson();
    @Test public void testBoolQuery_toJson();
    @Test public void testRangeQuery_toJson();
}
```

实现（GREEN）：
- `QueryBuilders`：静态工厂方法
- `MatchAllQueryBuilder`、`TermQueryBuilder`、`MatchQueryBuilder`
- `BoolQueryBuilder`（must/should/mustNot/filter）
- `RangeQueryBuilder`（gte/lte/gt/lt）
- `SearchSourceBuilder`：query + from + size + sort + aggregation

---

## Commit 计划
- `feat(client): add RestClient with HttpURLConnection`
- `feat(client): add RestHighLevelClient`
- `feat(client): add QueryBuilders and SearchSourceBuilder`
- `feat(client): add TransportClient`
