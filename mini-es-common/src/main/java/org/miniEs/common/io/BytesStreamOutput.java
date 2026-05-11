package org.miniEs.common.io;

import org.miniEs.common.bytes.BytesArray;
import org.miniEs.common.bytes.BytesReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * In-memory StreamOutput backed by a growing byte array.
 * Mirrors org.elasticsearch.common.io.stream.BytesStreamOutput.
 */
public class BytesStreamOutput extends StreamOutput {

    private final ByteArrayOutputStream baos;

    public BytesStreamOutput() {
        this.baos = new ByteArrayOutputStream();
    }

    public BytesStreamOutput(int initialCapacity) {
        this.baos = new ByteArrayOutputStream(initialCapacity);
    }

    @Override
    public void writeByte(byte b) throws IOException {
        baos.write(b & 0xFF);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        baos.write(b, offset, length);
    }

    public BytesReference bytes() {
        return new BytesArray(baos.toByteArray());
    }

    public int size() {
        return baos.size();
    }

    public void reset() {
        baos.reset();
    }
}
