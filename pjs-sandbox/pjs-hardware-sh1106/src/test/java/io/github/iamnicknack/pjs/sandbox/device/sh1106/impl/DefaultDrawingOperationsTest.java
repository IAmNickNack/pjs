package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import org.junit.jupiter.api.DynamicTest;
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
}