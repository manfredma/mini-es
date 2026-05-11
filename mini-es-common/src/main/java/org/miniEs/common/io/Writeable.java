package org.miniEs.common.io;

import java.io.IOException;

/**
 * Implemented by objects that can be serialized to a StreamOutput.
 * Mirrors org.elasticsearch.common.io.stream.Writeable.
 */
public interface Writeable {

    void writeTo(StreamOutput out) throws IOException;

    /**
     * Constructs an object from a StreamInput.
     * Mirrors org.elasticsearch.common.io.stream.Writeable.Reader.
     */
    @FunctionalInterface
    interface Reader<V> {
        V read(StreamInput in) throws IOException;
    }
}
