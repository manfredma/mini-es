# Phase 3: mini-es-examples 实现计划

## 目标
提供完整可运行的示例，演示 mini-ES 的主要功能，同时作为系统级集成测试。

---

## 任务清单

### P3-1: GettingStartedExample
**演示**：最简 CRUD 操作，适合新手快速上手

```java
public class GettingStartedExample {
    public static void main(String[] args) throws Exception {
        // 1. 启动嵌入式服务器
        Node node = new Node(NodeConfig.builder()
            .httpPort(9200)
            .transportPort(9300)
            .dataDir("./data")
            .build());
        node.start();

        // 2. 创建 RestHighLevelClient
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200))
        );

        // 3. 创建索引
        client.indices().create(new CreateIndexRequest("movies"), RequestOptions.DEFAULT);

        // 4. 写入文档
        IndexResponse indexResponse = client.index(
            new IndexRequest("movies").id("1")
                .source(Map.of(
                    "title", "The Shawshank Redemption",
                    "year", 1994,
                    "rating", 9.3
                )),
            RequestOptions.DEFAULT
        );
        System.out.println("Indexed: " + indexResponse.getResult());

        // 5. 读取文档
        GetResponse getResponse = client.get(
            new GetRequest("movies", "1"),
            RequestOptions.DEFAULT
        );
        System.out.println("Found: " + getResponse.getSourceAsMap());

        // 6. 更新文档
        client.index(
            new IndexRequest("movies").id("1")
                .source(Map.of("title", "Shawshank Redemption (Updated)", "year", 1994)),
            RequestOptions.DEFAULT
        );

        // 7. 删除文档
        client.delete(new DeleteRequest("movies", "1"), RequestOptions.DEFAULT);

        // 8. 清理
        client.close();
        node.stop();
    }
}
```

---

### P3-2: SearchExample
**演示**：全文搜索、精确匹配、布尔查询

```java
public class SearchExample {
    public static void main(String[] args) throws Exception {
        // 准备测试数据（10 部电影）
        // ...

        // 全文搜索
        SearchResponse resp1 = client.search(
            new SearchRequest("movies")
                .source(new SearchSourceBuilder()
                    .query(QueryBuilders.matchQuery("title", "redemption"))
                ),
            RequestOptions.DEFAULT
        );
        System.out.println("Match results: " + resp1.getHits().getTotalHits());

        // 精确匹配
        SearchResponse resp2 = client.search(
            new SearchRequest("movies")
                .source(new SearchSourceBuilder()
                    .query(QueryBuilders.termQuery("year", 1994))
                ),
            RequestOptions.DEFAULT
        );

        // 布尔查询
        SearchResponse resp3 = client.search(
            new SearchRequest("movies")
                .source(new SearchSourceBuilder()
                    .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title", "the"))
                        .filter(QueryBuilders.rangeQuery("rating").gte(8.0))
                    )
                ),
            RequestOptions.DEFAULT
        );

        // 分页
        SearchResponse resp4 = client.search(
            new SearchRequest("movies")
                .source(new SearchSourceBuilder()
                    .query(QueryBuilders.matchAllQuery())
                    .from(0).size(5)
                ),
            RequestOptions.DEFAULT
        );
    }
}
```

---

### P3-3: BulkExample
**演示**：批量写入性能和 API 用法

```java
public class BulkExample {
    public static void main(String[] args) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 1000; i++) {
            bulkRequest.add(
                new IndexRequest("products").id(String.valueOf(i))
                    .source(Map.of(
                        "name", "Product " + i,
                        "price", i * 9.99,
                        "category", i % 5 == 0 ? "electronics" : "clothing"
                    ))
            );
        }

        long start = System.currentTimeMillis();
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("Bulk indexed %d docs in %dms (%.0f docs/sec)%n",
            1000, elapsed, 1000.0 / elapsed * 1000);

        // 检查是否有失败项
        if (bulkResponse.hasFailures()) {
            System.out.println("Failures: " + bulkResponse.buildFailureMessage());
        }
    }
}
```

---

### P3-4: EmbeddedNodeExample（集成测试基类）
**演示**：如何在 JUnit 测试中嵌入 mini-ES

```java
public abstract class EmbeddedNodeTestCase {
    protected static Node node;
    protected static RestHighLevelClient client;

    @BeforeClass
    public static void startNode() throws Exception {
        node = new Node(NodeConfig.builder()
            .httpPort(0)          // 随机端口，避免冲突
            .transportPort(0)
            .dataDir(Files.createTempDirectory("mini-es-test").toString())
            .build());
        node.start();
        client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", node.httpPort()))
        );
    }

    @AfterClass
    public static void stopNode() throws Exception {
        if (client != null) client.close();
        if (node != null) node.stop();
    }
}

// 使用方式
public class MySearchTest extends EmbeddedNodeTestCase {
    @Before
    public void createIndex() throws Exception {
        client.indices().create(new CreateIndexRequest("test"), RequestOptions.DEFAULT);
    }

    @After
    public void deleteIndex() throws Exception {
        client.indices().delete(new DeleteIndexRequest("test"), RequestOptions.DEFAULT);
    }

    @Test
    public void testSearch() throws Exception {
        // ...
    }
}
```

---

### P3-5: CurlEquivalentExample
**演示**：用 TransportClient 直接发送 Action（等价于 curl）

```java
// 等价于: curl -X GET "localhost:9200/_cluster/health"
ClusterHealthResponse health = transportClient.execute(
    ClusterHealthAction.INSTANCE,
    new ClusterHealthRequest(),
    ActionListener.nowait()
).get();
System.out.println("Cluster status: " + health.getStatus());
```

---

## Commit 计划
- `feat(examples): add GettingStartedExample`
- `feat(examples): add SearchExample`
- `feat(examples): add BulkExample`
- `feat(examples): add EmbeddedNodeTestCase base class`
- `feat(examples): add CurlEquivalentExample`
