package org.miniEs.common.io;

import org.miniEs.common.bytes.BytesReference;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Stream output for serialization. Mirrors org.elasticsearch.common.io.stream.StreamOutput.
 */
public abstract class StreamOutput extends OutputStream {

    public abstract void writeByte(byte b) throws IOException;

    @Override
    public void write(int b) throws IOException {
        writeByte((byte) b);
    }

    public void writeBytes(byte[] b) throws IOException {
        writeBytes(b, 0, b.length);
    }

    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            writeByte(b[i]);
        }
    }

    public void writeInt(int v) throws IOException {
        writeByte((byte) (v >> 24));
        writeByte((byte) (v >> 16));
        writeByte((byte) (v >> 8));
        writeByte((byte) v);
    }

    public void writeLong(long v) throws IOException {
        writeInt((int) (v >> 32));
        writeInt((int) v);
    }

    public void writeShort(short v) throws IOException {
        writeByte((byte) (v >> 8));
        writeByte((byte) v);
    }

    public void writeBoolean(boolean b) throws IOException {
        writeByte(b ? (byte) 1 : (byte) 0);
    }

    /**
     * Variable-length integer encoding. Mirrors ES vInt encoding.
     * Values 0-127 use 1 byte; larger values use more bytes.
     */
    public void writeVInt(int v) throws IOException {
        while ((v & ~0x7F) != 0) {
            writeByte((byte) ((v & 0x7F) | 0x80));
            v >>>= 7;
        }
        writeByte((byte) v);
    }

    public void writeVLong(long v) throws IOException {
        while ((v & ~0x7FL) != 0L) {
            writeByte((byte) ((v & 0x7F) | 0x80));
            v >>>= 7;
        }
        writeByte((byte) v);
    }

    /**
     * Writes a UTF-8 string: 2-byte length (as vInt) + bytes.
     */
    public void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVInt(bytes.length);
        writeBytes(bytes);
    }

    public void writeOptionalString(String s) throws IOException {
        if (s == null) {
            writeBoolean(false);
        } else {
            writeBoolean(true);
            writeString(s);
        }
    }

    public void writeBytesReference(BytesReference ref) throws IOException {
        if (ref == null) {
            writeVInt(0);
            return;
        }
        byte[] bytes = ref.toBytes();
        writeVInt(bytes.length);
        writeBytes(bytes);
    }

    public <K, V> void writeMap(Map<K, V> map,
                                 Checkable<StreamOutput, K> keyWriter,
                                 Checkable<StreamOutput, V> valueWriter) throws IOException {
        writeVInt(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(this, entry.getKey());
            valueWriter.accept(this, entry.getValue());
        }
    }

    @FunctionalInterface
    public interface Checkable<T, V> {
        void accept(T t, V v) throws IOException;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
