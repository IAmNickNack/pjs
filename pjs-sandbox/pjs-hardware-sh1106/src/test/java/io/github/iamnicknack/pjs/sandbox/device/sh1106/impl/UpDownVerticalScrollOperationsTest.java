package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.VerticalScrollOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.UpDownScrollOperations.ShiftResult;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UpDownVerticalScrollOperationsTest {

    @Test
    void scrollsDataUpAcrossPages() {
        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        byte[] payload = new byte[DisplayOperations.PAGE_SIZE];
        Arrays.fill(payload, (byte)1);
        buf.setData(0, 0, payload, 0, payload.length);
        buf.setData(7, 0, payload, 0, payload.length);

        VerticalScrollOperations scrollUp = UpDownScrollOperations.scrollUp(buf);

        byte[] backPorch = scrollUp.scrollVertically(1);
        assertEquals((byte)0x80, backPorch[0]);

        byte[] seventhRow = new byte[DisplayOperations.PAGE_SIZE];
        buf.getData(6, 0, seventhRow, 0, seventhRow.length);
        assertEquals((byte)0x80, seventhRow[0]);
    }

    @Test
    void scrollsDataDownAcrossPages() {
        byte[] payload = new byte[DisplayOperations.PAGE_SIZE];
        Arrays.fill(payload, (byte)0x80);

        DefaultDisplayBuffer buf = new DefaultDisplayBuffer();
        buf.setData(0, 0, payload, 0, payload.length);
        buf.setData(7, 0, payload, 0, payload.length);

        VerticalScrollOperations scrollDown = UpDownScrollOperations.scrollDown(buf);

//        // front porch contains data shifted out
        byte[] frontPorch = scrollDown.scrollVertically(1);
        assertEquals((byte)1, frontPorch[0]);

        // row 1 contains data shifted in from row 0
        byte[] secondRow = new byte[DisplayOperations.PAGE_SIZE];
        buf.getData(1, 0, secondRow, 0, secondRow.length);
        assertEquals((byte)1, secondRow[0]);
    }

    @TestFactory
    Stream<DynamicTest> scrollUpOperation() {
        var operation = new UpDownScrollOperations.ScrollUpOperation();
        record Expectation(int current, int porch, int pixels, ShiftResult expected) {}

        return Stream.of(
                new Expectation(0, 0, 1, new ShiftResult(0, 0)),
                new Expectation(1, 0, 1, new ShiftResult(0, 0x80)),
                new Expectation(2, 0, 1, new ShiftResult(1, 0)),
                new Expectation(3, 0, 1, new ShiftResult(1, 0x80)),
                new Expectation(0, 1, 1, new ShiftResult(1, 0)),
                new Expectation(1, 1, 1, new ShiftResult(1, 0x80)),
                new Expectation(2, 1, 1, new ShiftResult(1, 0)),
                new Expectation(5, 1, 1, new ShiftResult(3, 0x80)),
                new Expectation(5, 1, 2, new ShiftResult(1, 0x40))
        ).map(e ->
                DynamicTest.dynamicTest(e.toString(), () ->
                        assertThat(operation.shift(e.current, e.porch, e.pixels)).isEqualTo(e.expected)
                )
        );
    }

    @TestFactory
    Stream<DynamicTest> scrollDownOperation() {
        var operation = new UpDownScrollOperations.ScrollDownOperation();
        record Expectation(int current, int porch, int pixels, ShiftResult expected) {}

        return Stream.of(
                new Expectation(0, 0, 1, new ShiftResult(0, 0)),
                new Expectation(1, 0, 1, new ShiftResult(2, 0)),
                new Expectation(2, 0, 1, new ShiftResult(4, 0)),
                new Expectation(3, 0, 1, new ShiftResult(6, 0)),
                new Expectation(0, 0x80, 1, new ShiftResult(0x80, 0)),
                new Expectation(1, 0x80, 1, new ShiftResult(0x82, 0)),
                new Expectation(2, 0x80, 1, new ShiftResult(0x84, 0)),
                new Expectation(3, 0x80, 1, new ShiftResult(0x86, 0)),
                new Expectation(0x80, 1, 1, new ShiftResult(1, 1)),
                new Expectation(0x80, 1, 2, new ShiftResult(1, 2))
        ).map(e ->
                DynamicTest.dynamicTest(e.toString(), () ->
                        assertThat(operation.shift(e.current, e.porch, e.pixels)).isEqualTo(e.expected)
                )
        );
    }

    @Test
    void canChainScrollOperations_byte() {
        chainScrollOperations(new DefaultDisplayBuffer(), new DefaultDisplayBuffer());
    }

    @Test
    void canChainScrollOperations_long() {
       chainScrollOperations(new LongDisplayBuffer(), new LongDisplayBuffer());
    }

    void chainScrollOperations(BufferedDisplayOperations bufferAbove, BufferedDisplayOperations bufferBelow) {
        // add data to the buffer below
        byte[] initialData = new byte[DisplayOperations.PAGE_SIZE];
        Arrays.fill(initialData, (byte)1);
        bufferBelow.setData(0, 0, initialData, 0, initialData.length);

        // assert that buffer above is empty
        byte[] result = new byte[DisplayOperations.PAGE_SIZE];
        bufferAbove.getData(7, 0, result, 0, result.length);
        assertEquals((byte)0, result[0]);

        // perform scroll up operation
        var scrollUpOperation = VerticalScrollOperations.composed(
                UpDownScrollOperations.scrollUp(bufferBelow),
                UpDownScrollOperations.scrollUp(bufferAbove)
        );
        scrollUpOperation.scrollVertically(1);

        // assert that buffer above now contains data shifted out of the buffer below
        bufferAbove.getData(7, 0, result, 0, result.length);
        assertEquals((byte)0x80, result[0]);

        // perform scroll up operation
        var scrollDownOperation = VerticalScrollOperations.composed(
                UpDownScrollOperations.scrollDown(bufferAbove),
                UpDownScrollOperations.scrollDown(bufferBelow)
        );
        scrollDownOperation.scrollVertically(1);

        // assert that buffer above is now empty
        bufferAbove.getData(7, 0, result, 0, result.length);
        assertEquals((byte)0, result[0]);

        // assert that buffer below contains data shifted in from the buffer above
        bufferBelow.getData(0, 0, result, 0, result.length);
        assertEquals((byte)1, result[0]);
    }
}
