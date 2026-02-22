package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultDrawingOperationsTest {

    @TestFactory
    Stream<DynamicTest> canDrawLine() {
        return Stream.of(
                new DefaultDisplayBuffer(),
                new StackedDisplayBuffer()
        ).map(buffer -> DynamicTest.dynamicTest(buffer.getClass().getSimpleName(), () -> {
            var drawingOperations = new DefaultDrawingOperations(buffer);

            drawingOperations.drawLine(0, 0, 0, 8);
            assertEquals(255, buffer.getPointValue(0, 0));

            drawingOperations.drawLine(1, 1, 1, 2);
            assertEquals(6, buffer.getPointValue(0, 1));
        }));
    }


    @Test
    void canDrawVerticalLine() {
        var buffer = new DefaultDisplayBuffer() {
            @Override
            public void orData (int page, int column, byte[] data, int offset, int length) {
                switch (page) {
                    case 0 -> assertEquals(0b1100_0000, data[0] & 0xff);
                    case 1 -> assertEquals(0b1111_1111, data[0] & 0xff);
                    case 2 -> assertEquals(0b0000_0011, data[0] & 0xff);
                    default -> throw new IllegalStateException();
                }
            }
        };

        var operations = new DefaultDrawingOperations(buffer);
        operations.drawVerticalLine(0, 6, 18);
    }
}