package org.miniEs.common.xcontent;

import java.io.IOException;

/**
 * Factory for creating XContent builders/parsers.
 * Mirrors org.elasticsearch.common.xcontent.XContentFactory.
 */
public class XContentFactory {

    public static XContentBuilder jsonBuilder() throws IOException {
        return new XContentBuilder();
    }
}
