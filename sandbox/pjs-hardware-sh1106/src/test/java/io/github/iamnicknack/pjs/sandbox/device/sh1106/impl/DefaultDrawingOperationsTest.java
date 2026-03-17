package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultDrawingOperationsTest {

    @TestFactory
    Stream<DynamicTest> canDrawVerticalLines() {
        record Expectation(int start, int end, int expected) {}
        return Stream.of(
                new Expectation(0, 7, 255),
                new Expectation(0, 1, 3),
                new Expectation(0, 4, 31),
                new Expectation(1, 7, 254),
                new Expectation(1, 2, 6),
                new Expectation(1, 5, 62),
                new Expectation(1, 4, 30)
        ).map(expectation -> DynamicTest.dynamicTest(expectation.toString(), () -> {
            var buffer = new DefaultDisplayBuffer();
            var drawingOperations = new DefaultDrawingOperations(buffer);

            drawingOperations.drawLine(0, expectation.start, 0, expectation.end);
            assertEquals(expectation.expected, buffer.getPointValue(0, 0));
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
                    case 2 -> assertEquals(0b0000_0111, data[0] & 0xff);
                    default -> throw new IllegalStateException();
                }
            }
        };

        var operations = new DefaultDrawingOperations(buffer);
        operations.drawVerticalLine(0, 6, 18);
    }
}