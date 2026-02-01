package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackedDisplayBufferTest {

    @Test
    void canSetPixel() {
        var buffer = new StackedDisplayBuffer();
        buffer.setData(0, 0, new byte[] { 0b00000001 }, 0, 1);
        assertEquals(1, buffer.getPointValue(0, 0));
    }

    @Test
    void canReadPixel() {
        var buffer = new StackedDisplayBuffer();
        buffer.setData(0, 0, new byte[] { 0b00000001 }, 0, 1);

        byte[] bytes = new byte[1];
        buffer.getData(0, 0, bytes, 0, 1);
        assertEquals(1, bytes[0]);
    }

    @Test
    void canSetMultiplePixels() {
        var buffer = new StackedDisplayBuffer();
        buffer.setData(0, 0, new byte[] { 0b00000011 }, 0, 1);
        assertEquals(3, buffer.getPointValue(0, 0));
    }

    @Test
    void canReadMultiplePixels() {
        var buffer = new StackedDisplayBuffer();
        buffer.setData(0, 0, new byte[] { 0b00000011 }, 0, 1);

        byte[] bytes = new byte[1];
        buffer.getData(0, 0, bytes, 0, 1);
        assertEquals(3, bytes[0]);
    }

    @Test
    void canSetMultipleGroupsOfPixels() {
        var buffer = new StackedDisplayBuffer();
        buffer.setData(0, 0, new byte[] { 0b00000011, 0b00000001 }, 0, 2);
        assertEquals(3, buffer.getPointValue(0, 0));
        assertEquals(1, buffer.getPointValue(0, 1));
    }

}