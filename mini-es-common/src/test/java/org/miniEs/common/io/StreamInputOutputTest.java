package org.miniEs.common.io;

import org.junit.Test;
import org.miniEs.common.bytes.BytesArray;
import org.miniEs.common.bytes.BytesReference;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StreamInputOutputTest {

    private StreamInput roundTrip(StreamOutput out) throws IOException {
        return StreamInput.wrap(((BytesStreamOutput) out).bytes().toBytes());
    }

    @Test
    public void testInt() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeInt(42);
        out.writeInt(-1);
        out.writeInt(Integer.MAX_VALUE);
        out.writeInt(Integer.MIN_VALUE);
        StreamInput in = roundTrip(out);
        assertEquals(42, in.readInt());
        assertEquals(-1, in.readInt());
        assertEquals(Integer.MAX_VALUE, in.readInt());
        assertEquals(Integer.MIN_VALUE, in.readInt());
    }

    @Test
    public void testLong() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeLong(Long.MAX_VALUE);
        out.writeLong(0L);
        out.writeLong(-999L);
        StreamInput in = roundTrip(out);
        assertEquals(Long.MAX_VALUE, in.readLong());
        assertEquals(0L, in.readLong());
        assertEquals(-999L, in.readLong());
    }

    @Test
    public void testBoolean() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeBoolean(true);
        out.writeBoolean(false);
        StreamInput in = roundTrip(out);
        assertTrue(in.readBoolean());
        assertFalse(in.readBoolean());
    }

    @Test
    public void testString() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeString("hello");
        out.writeString("");
        out.writeString("中文字符");
        StreamInput in = roundTrip(out);
        assertEquals("hello", in.readString());
        assertEquals("", in.readString());
        assertEquals("中文字符", in.readString());
    }

    @Test
    public void testOptionalString() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeOptionalString("value");
        out.writeOptionalString(null);
        StreamInput in = roundTrip(out);
        assertEquals("value", in.readOptionalString());
        assertNull(in.readOptionalString());
    }

    @Test
    public void testBytesReference() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeBytesReference(new BytesArray(data));
        StreamInput in = roundTrip(out);
        BytesReference result = in.readBytesReference();
        assertArrayEquals(data, result.toBytes());
    }

    @Test
    public void testVInt() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeVInt(0);
        out.writeVInt(127);
        out.writeVInt(128);
        out.writeVInt(16383);
        out.writeVInt(Integer.MAX_VALUE);
        StreamInput in = roundTrip(out);
        assertEquals(0, in.readVInt());
        assertEquals(127, in.readVInt());
        assertEquals(128, in.readVInt());
        assertEquals(16383, in.readVInt());
        assertEquals(Integer.MAX_VALUE, in.readVInt());
    }

    @Test
    public void testMap() throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeMap(map, StreamOutput::writeString, StreamOutput::writeString);
        StreamInput in = roundTrip(out);
        Map<String, String> result = in.readMap(StreamInput::readString, StreamInput::readString);
        assertEquals(map, result);
    }

    @Test
    public void testByte() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        out.writeByte((byte) 0xFF);
        out.writeByte((byte) 0x00);
        StreamInput in = roundTrip(out);
        assertEquals((byte) 0xFF, in.readByte());
        assertEquals((byte) 0x00, in.readByte());
    }
}
