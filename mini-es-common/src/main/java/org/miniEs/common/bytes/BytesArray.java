package org.miniEs.common.bytes;

import java.util.Arrays;

/**
 * Default implementation of BytesReference backed by a byte array.
 * Mirrors org.elasticsearch.common.bytes.BytesArray.
 */
public class BytesArray implements BytesReference {

    private final byte[] bytes;
    private final int offset;
    private final int length;

    public BytesArray(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public BytesArray(byte[] bytes, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException(
                "Invalid offset/length: offset=" + offset + " length=" + length + " array.length=" + bytes.length);
        }
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public byte get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index=" + index + " length=" + length);
        }
        return bytes[offset + index];
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public BytesReference slice(int from, int len) {
        if (from < 0 || len < 0 || from + len > length) {
            throw new IllegalArgumentException(
                "Invalid slice: from=" + from + " length=" + len + " this.length=" + length);
        }
        return new BytesArray(bytes, offset + from, len);
    }

    @Override
    public byte[] toBytes() {
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BytesReference)) return false;
        BytesReference other = (BytesReference) o;
        if (length != other.length()) return false;
        for (int i = 0; i < length; i++) {
            if (get(i) != other.get(i)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < length; i++) {
            result = 31 * result + bytes[offset + i];
        }
        return result;
    }

    @Override
    public String toString() {
        return new String(toBytes());
    }
}
