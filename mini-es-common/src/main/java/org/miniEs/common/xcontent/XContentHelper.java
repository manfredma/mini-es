package org.miniEs.common.xcontent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miniEs.common.bytes.BytesReference;

import java.io.IOException;
import java.util.Map;

/**
 * Utility methods for XContent operations.
 * Mirrors org.elasticsearch.common.xcontent.XContentHelper.
 */
public class XContentHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertToMap(BytesReference ref) throws IOException {
        return OBJECT_MAPPER.readValue(ref.toBytes(), Map.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertToMap(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, Map.class);
    }

    public static String convertToJson(Map<String, Object> map) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(map);
    }

    public static BytesReference toXContent(Map<String, Object> map) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(map);
        return org.miniEs.common.bytes.BytesReference.fromBytes(bytes);
    }
}
