package org.miniEs.common.xcontent;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.miniEs.common.bytes.BytesArray;
import org.miniEs.common.bytes.BytesReference;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Builds JSON content incrementally.
 * Mirrors org.elasticsearch.common.xcontent.XContentBuilder.
 */
public class XContentBuilder implements Closeable {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private final ByteArrayOutputStream baos;
    private final JsonGenerator generator;

    XContentBuilder() throws IOException {
        this.baos = new ByteArrayOutputStream();
        this.generator = JSON_FACTORY.createGenerator(baos);
    }

    public XContentBuilder startObject() throws IOException {
        generator.writeStartObject();
        return this;
    }

    public XContentBuilder startObject(String name) throws IOException {
        generator.writeObjectFieldStart(name);
        return this;
    }

    public XContentBuilder endObject() throws IOException {
        generator.writeEndObject();
        return this;
    }

    public XContentBuilder startArray() throws IOException {
        generator.writeStartArray();
        return this;
    }

    public XContentBuilder startArray(String name) throws IOException {
        generator.writeArrayFieldStart(name);
        return this;
    }

    public XContentBuilder endArray() throws IOException {
        generator.writeEndArray();
        return this;
    }

    public XContentBuilder field(String name) throws IOException {
        generator.writeFieldName(name);
        return this;
    }

    public XContentBuilder field(String name, String value) throws IOException {
        generator.writeStringField(name, value);
        return this;
    }

    public XContentBuilder field(String name, int value) throws IOException {
        generator.writeNumberField(name, value);
        return this;
    }

    public XContentBuilder field(String name, long value) throws IOException {
        generator.writeNumberField(name, value);
        return this;
    }

    public XContentBuilder field(String name, double value) throws IOException {
        generator.writeNumberField(name, value);
        return this;
    }

    public XContentBuilder field(String name, boolean value) throws IOException {
        generator.writeBooleanField(name, value);
        return this;
    }

    public XContentBuilder field(String name, Object value) throws IOException {
        generator.writeFieldName(name);
        writeValue(value);
        return this;
    }

    public XContentBuilder nullField(String name) throws IOException {
        generator.writeNullField(name);
        return this;
    }

    public XContentBuilder value(String value) throws IOException {
        generator.writeString(value);
        return this;
    }

    public XContentBuilder value(Object value) throws IOException {
        writeValue(value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public XContentBuilder map(Map<String, ?> map) throws IOException {
        generator.writeStartObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            generator.writeFieldName(entry.getKey());
            writeValue(entry.getValue());
        }
        generator.writeEndObject();
        return this;
    }

    @SuppressWarnings("unchecked")
    private void writeValue(Object value) throws IOException {
        if (value == null) {
            generator.writeNull();
        } else if (value instanceof String) {
            generator.writeString((String) value);
        } else if (value instanceof Integer) {
            generator.writeNumber((int) value);
        } else if (value instanceof Long) {
            generator.writeNumber((long) value);
        } else if (value instanceof Double) {
            generator.writeNumber((double) value);
        } else if (value instanceof Float) {
            generator.writeNumber((float) value);
        } else if (value instanceof Boolean) {
            generator.writeBoolean((boolean) value);
        } else if (value instanceof Map) {
            generator.writeStartObject();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                generator.writeFieldName(entry.getKey());
                writeValue(entry.getValue());
            }
            generator.writeEndObject();
        } else if (value instanceof List) {
            generator.writeStartArray();
            for (Object item : (List<?>) value) {
                writeValue(item);
            }
            generator.writeEndArray();
        } else {
            generator.writeString(value.toString());
        }
    }

    public BytesReference bytes() throws IOException {
        generator.flush();
        return new BytesArray(baos.toByteArray());
    }

    @Override
    public String toString() {
        try {
            return bytes().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }
}
