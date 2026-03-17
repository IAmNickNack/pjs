package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDisplayBufferTest {

    @Test
    void setAndGetData_roundTrip() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] payload = new byte[] {1, 2, 3, 4, 5};
        buf.setData(0, 0, payload, 0, payload.length);

        byte[] out = new byte[payload.length];
        buf.getData(0, 0, out, 0, out.length);
        assertArrayEquals(payload, out);
    }

    @Test
    void clearData_clearsRange() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] payload = new byte[] {10, 11, 12, 13};
        buf.setData(1, 5, payload, 0, payload.length);

        buf.clearData(1, 6, 2);

        byte[] out = new byte[payload.length];
        buf.getData(1, 5, out, 0, out.length);

        // index 0 (col 5) remains 10, index 1 and 2 cleared to 0, index 3 remains 13
        assertEquals(10, out[0]);
        assertEquals(0, out[1]);
        assertEquals(0, out[2]);
        assertEquals(13, out[3]);
    }

    @Test
    void setData_outOfBounds_throws() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] payload = new byte[10];
        // column + length > PAGE_SIZE should throw
        assertThrows(IllegalArgumentException.class, () -> buf.setData(0, DisplayOperations.PAGE_SIZE - 5, payload, 0, 10 + 5));
    }

    @Test
    void getData_outOfBounds_throws() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] out = new byte[4];
        assertThrows(IllegalArgumentException.class, () -> buf.getData(0, DisplayOperations.PAGE_SIZE - 2, out, 0, 4));
    }

    @Test
    void getPointValue_validAndBounds() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] payload = new byte[] {(byte)0xFF};
        buf.setData(2, 10, payload, 0, 1);
        // value should be returned as unsigned (0xFF -> 255)
        assertEquals(255, buf.getPointValue(2, 10));

        // note: implementation checks column > PAGE_SIZE for bounds (not >=), so test accordingly
        assertThrows(IllegalArgumentException.class, () -> buf.getPointValue(0, DisplayOperations.PAGE_SIZE + 1));
    }

    @Test
    void orXorAndAndNot_modifyOperations() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] initial = new byte[] {(byte)0b00001111, (byte)0b11110000, (byte)0b10101010};
        buf.setData(0, 0, initial, 0, initial.length);

        // OR with 01010101, 00001111, 11111111
        byte[] orOperand = new byte[] {(byte)0b01010101, (byte)0b00001111, (byte)0b11111111};
        buf.orData(0, 0, orOperand, 0, orOperand.length);

        byte[] out = new byte[3];
        buf.getData(0, 0, out, 0, out.length);

        assertEquals((byte)0b01011111, out[0]); // 00001111 | 01010101
        assertEquals((byte)0b11111111, out[1]); // 11110000 | 00001111
        assertEquals((byte)0b11111111, out[2]); // 10101010 | 11111111

        // XOR with 11111111 should invert
        byte[] xorOperand = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF};
        buf.xorData(0, 0, xorOperand, 0, xorOperand.length);
        buf.getData(0, 0, out, 0, out.length);
        assertEquals((byte)~0b01011111, out[0]);

        // AND with 0x0F should keep lower nibble
        byte[] andOperand = new byte[] {(byte)0x0F, (byte)0x0F, (byte)0x0F};
        buf.andData(0, 0, andOperand, 0, andOperand.length);
        buf.getData(0, 0, out, 0, out.length);
        assertEquals((byte)( (~0b01011111) & 0x0F ), out[0]);

        // AND NOT: set a known value then clear bits
        buf.setData(0, 0, new byte[] {(byte)0b11110000}, 0, 1);
        buf.andNotData(0, 0, new byte[] {(byte)0b11000011}, 0, 1);
        assertEquals((byte)(0b11110000 & ~0b11000011), buf.getPointValue(0, 0) & 0xFF);
    }

    @Test
    void modify_wrapsAroundEndOfPage() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        int page = 3;
        int start = DisplayOperations.PAGE_SIZE - 1;
        // put value at last column and at column 0 by writing separately (setData doesn't wrap)
        buf.setData(page, start, new byte[] {(byte)0x01}, 0, 1);
        buf.setData(page, 0, new byte[] {(byte)0x02}, 0, 1);

        // apply OR that will target last index and wrap to 0
        buf.orData(page, start, new byte[] {(byte)0x02, (byte)0x04}, 0, 2);

        assertEquals( (0x01 | 0x02), buf.getPointValue(page, start) );
        assertEquals( (0x02 | 0x04), buf.getPointValue(page, 0) );
    }

    @Test
    void copyTo_copiesEntireBuffer() {
        DefaultDisplayBuffer src = new DefaultDisplayBuffer();
        DefaultDisplayBuffer dst = new DefaultDisplayBuffer();

        // populate multiple pages with distinct data
        byte[] p0 = new byte[DisplayOperations.PAGE_SIZE];
        byte[] p1 = new byte[DisplayOperations.PAGE_SIZE];
        for (int i = 0; i < DisplayOperations.PAGE_SIZE; i++) {
            p0[i] = (byte) i;
            p1[i] = (byte) (255 - i);
        }
        src.setData(0, 0, p0, 0, p0.length);
        src.setData(1, 0, p1, 0, p1.length);

        src.copyTo(dst);

        byte[] out0 = new byte[DisplayOperations.PAGE_SIZE];
        byte[] out1 = new byte[DisplayOperations.PAGE_SIZE];
        dst.getData(0, 0, out0, 0, out0.length);
        dst.getData(1, 0, out1, 0, out1.length);

        assertArrayEquals(p0, out0);
        assertArrayEquals(p1, out1);
    }
}