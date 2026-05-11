# Phase 0: mini-es-common 实现计划

## 目标
构建 mini-es-common 模块，提供所有子模块共享的数据结构、序列化工具和基础异常体系。

## 任务清单

### P0-1: 项目骨架（Maven 多模块 POM）
- [ ] 创建 parent pom.xml（Java 8，JUnit 4，Jackson 2.13）
- [ ] 创建 mini-es-common/pom.xml
- [ ] 创建 mini-es-server/pom.xml（依赖 common）
- [ ] 创建 mini-es-client/pom.xml（依赖 common）
- [ ] 创建 mini-es-examples/pom.xml（依赖 client）

**TDD 节奏**：先写 pom，编译通过即绿灯。

---

### P0-2: BytesReference / BytesArray
**对齐**：`org.elasticsearch.common.bytes.BytesReference`

测试先行（RED）：
```java
public class BytesArrayTest {
    @Test public void testSlice();
    @Test public void testToBytes();
    @Test public void testEquals();
}
```

实现（GREEN）：
- `BytesReference`：接口，`length()`、`get(int)`、`slice(int, int)`、`toBytesRef()`
- `BytesArray`：byte[] 包装，实现 BytesReference

---

### P0-3: StreamInput / StreamOutput
**对齐**：`org.elasticsearch.common.io.stream.StreamInput/StreamOutput`

测试先行（RED）：
```java
public class StreamInputOutputTest {
    @Test public void testInt();
    @Test public void testLong();
    @Test public void testString();
    @Test public void testOptionalString();
    @Test public void testBytesReference();
    @Test public void testVInt();       // variable-length int（对齐 ES vInt）
    @Test public void testMap();
}
```

实现（GREEN）：
- `StreamOutput`：包装 `DataOutputStream` 或 `ByteArrayOutputStream`
- `StreamInput`：包装 `DataInputStream` 或 `ByteArrayInputStream`
- `BytesStreamOutput`：ES 中用于内存序列化的实现

---

### P0-4: XContentBuilder / XContentParser（JSON 封装）
**对齐**：`org.elasticsearch.common.xcontent`

测试先行（RED）：
```java
public class XContentBuilderTest {
    @Test public void testBuildObject();
    @Test public void testBuildArray();
    @Test public void testNestedObject();
    @Test public void testBytesToString();
}
public class XContentParserTest {
    @Test public void testParseField();
    @Test public void testParseArray();
    @Test public void testParseMap();
}
```

实现（GREEN）：
- `XContentType`：枚举（JSON），含 `mediaType()`
- `XContentBuilder`：包装 Jackson `ObjectNode`
- `XContentParser`：包装 Jackson `JsonParser`
- `XContentHelper`：`toMap(BytesReference)` 工具方法

---

### P0-5: Settings
**对齐**：`org.elasticsearch.common.settings.Settings`

测试先行（RED）：
```java
public class SettingsTest {
    @Test public void testGetString();
    @Test public void testGetInt();
    @Test public void testGetBoolean();
    @Test public void testBuilder();
    @Test public void testMerge();
}
```

实现（GREEN）：
- `Settings`：不可变 `Map<String, String>` 包装
- `Settings.Builder`：流式 builder

---

### P0-6: TimeValue / ByteSizeValue
**对齐**：`org.elasticsearch.common.unit.TimeValue`

测试先行（RED）：
```java
public class TimeValueTest {
    @Test public void testMillis();
    @Test public void testSeconds();
    @Test public void testParse();
    @Test public void testToString();  // "1s", "500ms"
}
```

---

### P0-7: 基础异常体系
**对齐**：`org.elasticsearch.ElasticsearchException`

实现（无需 TDD，直接写）：
```
ElasticsearchException (RuntimeException)
├── IndexNotFoundException
├── DocumentNotFoundException
├── IndexAlreadyExistsException
├── VersionConflictEngineException
├── SearchPhaseExecutionException
└── TransportException
    ├── ConnectTransportException
    └── SendRequestTransportException
```

每个异常包含：
- `getMessage()` 返回描述性信息
- `status()` 返回对应 HTTP 状态码

---

### P0-8: Writeable 接口
**对齐**：`org.elasticsearch.common.io.stream.Writeable`

```java
public interface Writeable {
    void writeTo(StreamOutput out) throws IOException;

    interface Reader<V> {
        V read(StreamInput in) throws IOException;
    }
}
```

---

## Commit 计划
每完成一个任务立即 commit：
- `chore: initialize maven multi-module project structure`
- `feat(common): add BytesReference and BytesArray`
- `feat(common): add StreamInput/StreamOutput serialization`
- `feat(common): add XContent JSON builder and parser`
- `feat(common): add Settings`
- `feat(common): add TimeValue`
- `feat(common): add exception hierarchy`
- `feat(common): add Writeable interface`
