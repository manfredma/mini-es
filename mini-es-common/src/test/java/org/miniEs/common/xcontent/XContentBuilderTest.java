package org.miniEs.common.xcontent;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class XContentBuilderTest {

    @Test
    public void testBuildSimpleObject() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
            .field("name", "Alice")
            .field("age", 30)
            .endObject();
        String json = builder.toString();
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    public void testBuildArray() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
            .field("tags")
            .startArray()
                .value("java")
                .value("elasticsearch")
            .endArray()
        .endObject();
        String json = builder.toString();
        assertTrue(json.contains("\"tags\""));
        assertTrue(json.contains("\"java\""));
        assertTrue(json.contains("\"elasticsearch\""));
    }

    @Test
    public void testNestedObject() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
            .field("name", "Bob")
            .startObject("address")
                .field("city", "Shanghai")
                .field("zip", "200000")
            .endObject()
        .endObject();
        String json = builder.toString();
        assertTrue(json.contains("\"address\""));
        assertTrue(json.contains("\"city\":\"Shanghai\""));
    }

    @Test
    public void testBooleanAndNull() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
            .field("active", true)
            .nullField("deleted")
        .endObject();
        String json = builder.toString();
        assertTrue(json.contains("\"active\":true"));
        assertTrue(json.contains("\"deleted\":null"));
    }

    @Test
    public void testMap() throws IOException {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("title", "Test Document");
        source.put("count", 42L);
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.map(source);
        String json = builder.toString();
        assertTrue(json.contains("\"title\":\"Test Document\""));
        assertTrue(json.contains("\"count\":42"));
    }
}
