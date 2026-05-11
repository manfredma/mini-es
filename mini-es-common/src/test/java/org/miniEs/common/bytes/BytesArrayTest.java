package org.miniEs.common.bytes;

import org.junit.Test;

import static org.junit.Assert.*;

public class BytesArrayTest {

    @Test
    public void testLength() {
        byte[] data = {1, 2, 3, 4, 5};
        BytesArray ba = new BytesArray(data);
        assertEquals(5, ba.length());
    }

    @Test
    public void testGet() {
        byte[] data = {10, 20, 30};
        BytesArray ba = new BytesArray(data);
        assertEquals(10, ba.get(0));
        assertEquals(20, ba.get(1));
        assertEquals(30, ba.get(2));
    }

    @Test
    public void testSlice() {
        byte[] data = {1, 2, 3, 4, 5};
        BytesArray ba = new BytesArray(data);
        BytesReference sliced = ba.slice(1, 3);
        assertEquals(3, sliced.length());
        assertEquals(2, sliced.get(0));
        assertEquals(3, sliced.get(1));
        assertEquals(4, sliced.get(2));
    }

    @Test
    public void testToBytes() {
        byte[] data = {7, 8, 9};
        BytesArray ba = new BytesArray(data);
        byte[] result = ba.toBytes();
        assertArrayEquals(data, result);
    }

    @Test
    public void testToBytesReturnsCopy() {
        byte[] data = {1, 2, 3};
        BytesArray ba = new BytesArray(data);
        byte[] copy = ba.toBytes();
        copy[0] = 99;
        assertEquals(1, ba.get(0)); // original not mutated
    }

    @Test
    public void testEquals() {
        byte[] data1 = {1, 2, 3};
        byte[] data2 = {1, 2, 3};
        BytesArray ba1 = new BytesArray(data1);
        BytesArray ba2 = new BytesArray(data2);
        assertEquals(ba1, ba2);
    }

    @Test
    public void testNotEquals() {
        BytesArray ba1 = new BytesArray(new byte[]{1, 2, 3});
        BytesArray ba2 = new BytesArray(new byte[]{1, 2, 4});
        assertNotEquals(ba1, ba2);
    }

    @Test
    public void testEmptyArray() {
        BytesArray ba = new BytesArray(new byte[0]);
        assertEquals(0, ba.length());
    }

    @Test
    public void testFromString() {
        BytesArray ba = new BytesArray("hello".getBytes());
        assertEquals(5, ba.length());
    }
}
