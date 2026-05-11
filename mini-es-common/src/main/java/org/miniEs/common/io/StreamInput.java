package org.miniEs.common.io;

import org.miniEs.common.bytes.BytesArray;
import org.miniEs.common.bytes.BytesReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stream input for deserialization. Mirrors org.elasticsearch.common.io.stream.StreamInput.
 */
public abstract class StreamInput extends InputStream {

    public abstract byte readByte() throws IOException;

    @Override
    public int read() throws IOException {
        return readByte() & 0xFF;
    }

    public void readBytes(byte[] b, int offset, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            b[offset + i] = readByte();
        }
    }

    public int readInt() throws IOException {
        return ((readByte() & 0xFF) << 24)
                | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) << 8)
                | (readByte() & 0xFF);
    }

    public long readLong() throws IOException {
        return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    public short readShort() throws IOException {
        return (short) (((readByte() & 0xFF) << 8) | (readByte() & 0xFF));
    }

    public boolean readBoolean() throws IOException {
        return readByte() != 0;
    }

    /**
     * Variable-length integer decoding. Mirrors ES vInt decoding.
     */
    public int readVInt() throws IOException {
        byte b = readByte();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    public long readVLong() throws IOException {
        byte b = readByte();
        long i = b & 0x7FL;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7FL) << shift;
        }
        return i;
    }

    public String readString() throws IOException {
        int len = readVInt();
        byte[] bytes = new byte[len];
        readBytes(bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String readOptionalString() throws IOException {
        if (readBoolean()) {
            return readString();
        }
        return null;
    }

    public BytesReference readBytesReference() throws IOException {
        int len = readVInt();
        if (len == 0) {
            return BytesReference.EMPTY;
        }
        byte[] bytes = new byte[len];
        readBytes(bytes, 0, len);
        return new BytesArray(bytes);
    }

    public <K, V> Map<K, V> readMap(CheckedFunction<StreamInput, K> keyReader,
                                      CheckedFunction<StreamInput, V> valueReader) throws IOException {
        int size = readVInt();
        Map<K, V> map = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            K key = keyReader.apply(this);
            V value = valueReader.apply(this);
            map.put(key, value);
        }
        return map;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws IOException;
    }

    public static StreamInput wrap(byte[] bytes) {
        return new ByteArrayStreamInput(bytes);
    }

    private static class ByteArrayStreamInput extends StreamInput {
        private final byte[] buf;
        private int pos;

        ByteArrayStreamInput(byte[] buf) {
            this.buf = buf;
            this.pos = 0;
        }

        @Override
        public byte readByte() throws IOException {
            if (pos >= buf.length) {
                throw new IOException("Unexpected end of stream at position " + pos);
            }
            return buf[pos++];
        }

        @Override
        public int available() {
            return buf.length - pos;
        }
    }
}
