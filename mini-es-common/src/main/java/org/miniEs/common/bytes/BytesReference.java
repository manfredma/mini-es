package org.miniEs.common.bytes;

/**
 * Immutable reference to a byte sequence. Mirrors org.elasticsearch.common.bytes.BytesReference.
 */
public interface BytesReference {

    byte get(int index);

    int length();

    BytesReference slice(int from, int length);

    byte[] toBytes();

    static BytesReference fromBytes(byte[] bytes) {
        return new BytesArray(bytes);
    }

    static BytesReference EMPTY = new BytesArray(new byte[0]);
}
