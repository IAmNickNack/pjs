package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import org.junit.jupiter.api.Test;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations.PAGE_COUNT;
import static io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations.PAGE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LongDisplayBufferTest {

    @Test
    void canSetAndModifyData() {
        var buffer = new LongDisplayBuffer();
        for (int page = 0; page < PAGE_COUNT; page++) {
            for (int column = 0; column < PAGE_SIZE; column++) {
                buffer.setData(page, column, new byte[]{1}, 0, 1);
                assertEquals(1, buffer.getPointValue(page, column));

                buffer.andData(page, column, new byte[]{1}, 0, 1);
                assertEquals(1, buffer.getPointValue(page, column));

                buffer.andData(page, column, new byte[]{2}, 0, 1);
                assertEquals(0, buffer.getPointValue(page, column));

                buffer.orData(page, column, new byte[]{4}, 0, 1);
                assertEquals(4, buffer.getPointValue(page, column));

                buffer.orData(page, column, new byte[]{2}, 0, 1);
                assertEquals(6, buffer.getPointValue(page, column));

                buffer.xorData(page, column, new byte[]{2}, 0, 1);
                assertEquals(4, buffer.getPointValue(page, column));

                buffer.orData(page, column, new byte[]{1}, 0, 1);
                assertEquals(5, buffer.getPointValue(page, column));
                buffer.andNotData(page, column, new byte[]{1}, 0, 1);
                assertEquals(4, buffer.getPointValue(page, column));
            }
        }
    }

    @Test
    void canScrollUp() {
        var buffer = new LongDisplayBuffer();
        var data = new byte[PAGE_SIZE];
        for (int i = 0; i < PAGE_SIZE; i++) {
            data[i] = 1;
        }
        buffer.setData(0, 0, data, 0, PAGE_SIZE);

        data = buffer.scrollUpOperations().scrollVertically(1);
        assertEquals((byte)0x80, data[0]);
    }

    @Test
    void canRotateUp() {
        var buffer = new LongDisplayBuffer();
        var data = new byte[PAGE_SIZE];
        for (int i = 0; i < PAGE_SIZE; i++) {
            data[i] = 1;
        }
        buffer.setData(0, 0, data, 0, PAGE_SIZE);

        data = buffer.rotateUpOperations().scrollVertically(1);
        assertEquals((byte)0x80, data[0]);

        assertEquals(1L << 63, buffer.getColumn(0));
    }

    @Test
    void canScrollDown() {
        var buffer = new LongDisplayBuffer();
        var data = new byte[PAGE_SIZE];
        for (int i = 0; i < PAGE_SIZE; i++) {
            data[i] = (byte)0x80;
        }
        buffer.setData(0, 0, data, 0, PAGE_SIZE);
        buffer.setData(7, 0, data, 0, PAGE_SIZE);

        data = buffer.scrollDownOperations().scrollVertically(1);
        assertEquals((byte)1, data[0]);

        buffer.getData(1, 0, data, 0, PAGE_SIZE);
        assertEquals((byte)1, data[0]);
    }

    @Test
    void canRotateDown() {
        var buffer = new LongDisplayBuffer();
        var data = new byte[PAGE_SIZE];
        for (int i = 0; i < PAGE_SIZE; i++) {
            data[i] = (byte)0x80;
        }
        buffer.setData(7, 0, data, 0, PAGE_SIZE);

        data = buffer.rotateDownOperations().scrollVertically(1);
        assertEquals((byte)1, data[0]);

        assertEquals(1L, buffer.getColumn(0));
    }
}
