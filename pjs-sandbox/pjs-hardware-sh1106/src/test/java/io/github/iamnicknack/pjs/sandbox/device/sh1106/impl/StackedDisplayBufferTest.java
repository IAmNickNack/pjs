package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

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

    @Test
    void canSetPixelValueMultipleTimes() {
        var buffer = new StackedDisplayBuffer();
        buffer.addData(0, 0, new byte[] { 0b00000001 }, 0, 1);

        byte[] bytes = new byte[1];
        buffer.getData(0, 0, bytes, 0, 1);
        assertEquals(1, bytes[0]);

        buffer.addData(0, 0, new byte[] { 0b00000001 }, 0, 1);

        var group = buffer.getPixelGroup(0, 0);
        assertEquals(2, group.getPlotCount(0));
    }

    @Test
    void canUnsetPixelValue() {
        var buffer = new StackedDisplayBuffer();
        buffer.addData(0, 0, new byte[] { 0b00000001 }, 0, 1);
        buffer.addData(0, 0, new byte[] { 0b00000001 }, 0, 1);

        var group = buffer.getPixelGroup(0, 0);
        assertEquals(2, group.getPlotCount(0));

        buffer.removeData(0, 0, new byte[] { 0b00000001 }, 0, 1);
        assertEquals(1, group.getPlotCount(0));

        buffer.removeData(0, 0, new byte[] { 0b00000001 }, 0, 1);
        assertEquals(0, group.getPlotCount(0));

        buffer.removeData(0, 0, new byte[] { 0b00000001 }, 0, 1);
        assertEquals(0, group.getPlotCount(0));
    }

    @Test
    void canDrawLine() {
        var buffer = new StackedDisplayBuffer();
        var additive = buffer.additive();
        var subtractive = buffer.subtractive();

        var group = buffer.getPixelGroup(0, 0);

        var drawOperations = new DefaultDrawingOperations(additive);
        drawOperations.drawLine(0, 0, 0, 8);

        for (int i = 0; i < 8; i++) {
            assertEquals(1, group.getPlotCount(i));
        }

        var eraseOperations = new DefaultDrawingOperations(subtractive);
        eraseOperations.drawLine(0, 0, 0, 8);

        for (int i = 0; i < 8; i++) {
            assertEquals(0, group.getPlotCount(i));
        }
    }
}
