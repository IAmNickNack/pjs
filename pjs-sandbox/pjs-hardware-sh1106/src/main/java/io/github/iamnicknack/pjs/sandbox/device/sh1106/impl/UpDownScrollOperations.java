package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.VerticalScrollOperations;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations.PAGE_SIZE;

/**
 * Provides scrolling operations for a display buffer.
 * <p>
 * Delegates to {@link ShiftOperation} implementations to perform the actual shifting, allowing the workflow to be
 * generalised.
 */
public class UpDownScrollOperations implements VerticalScrollOperations {

    /**
     * Create a scroll operation that scrolls up.
     * @param displayOperations The display operations to use.
     * @return The scroll operation.
     */
    public static VerticalScrollOperations scrollUp(BufferedDisplayOperations displayOperations) {
        return new UpDownScrollOperations(
                displayOperations,
                new ScrollUpOperation(),
                () -> IntStream.iterate(7, i -> i - 1).limit(8)
        );
    }

    /**
     * Create a scroll operation that scrolls down.
     * @param displayOperations The display operations to use.
     * @return The scroll operation.
     */
    public static VerticalScrollOperations scrollDown(BufferedDisplayOperations displayOperations) {
        return new UpDownScrollOperations(
                displayOperations,
                new ScrollDownOperation(),
                () -> IntStream.range(0, 8)
        );
    }

    /**
     * Delegate display operations.
     */
    private final BufferedDisplayOperations displayOperations;
    /**
     * The {@link ShiftOperation} to use for shifting the data.
     */
    private final ShiftOperation shiftOperation;
    /**
     * The {@link Supplier} of {@link IntStream} that provides the page indices for scrolling.
     */
    private final Supplier<IntStream> rangeSupplier;

    /**
     * Constructor.
     * @param displayOperations the delegate display operations being scrolled
     * @param shiftOperation the {@link ShiftOperation} to use for shifting the data.
     * @param rangeSupplier the {@link Supplier} of {@link IntStream} that provides the page indices for scrolling.
     */
    private UpDownScrollOperations(
            BufferedDisplayOperations displayOperations,
            ShiftOperation shiftOperation,
            Supplier<IntStream> rangeSupplier
    ) {
        this.displayOperations = displayOperations;
        this.shiftOperation = shiftOperation;
        this.rangeSupplier = rangeSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] scrollVertically(int pixels, byte[] initialPorch) {
        byte[] frontPorch = new byte[PAGE_SIZE];
        System.arraycopy(initialPorch, 0, frontPorch, 0, PAGE_SIZE);
        rangeSupplier.get().forEach(page -> {
            byte[] bufferPage = new byte[PAGE_SIZE];
            this.displayOperations.getData(page, 0, bufferPage, 0, PAGE_SIZE);

            for (int column = 0; column < PAGE_SIZE; column++) {
                int currentValue = bufferPage[column] & 0xFF;
                int porchValue = frontPorch[column] & 0xFF;

                ShiftResult result = shiftOperation.shift(currentValue, porchValue, pixels);
                bufferPage[column] = result.value;
                frontPorch[column] = result.porch;
            }

            this.displayOperations.setData(page, 0, bufferPage, 0, PAGE_SIZE);
        });

        return frontPorch;
    }

    /**
     * An operation that shifts the data by the specified number of pixels, applying the carry bit(s) if necessary.
     */
    interface ShiftOperation {
        ShiftResult shift(int currentValue, int porchValue, int pixels);
    }

    /**
     * Operations which can shift data to affect `scroll down`.
     * <ul>
     *     <li>The current value is shifted left by the specified number of pixels.</li>
     *     <li>The carry is returned in the porch value.</li>
     *     <li>The porch value is or-ed with the new value.</li>
     * </ul>
     */
    static class ScrollDownOperation implements ShiftOperation {
        @Override
        public ShiftResult shift(int currentValue, int porchValue, int pixels) {
            int value = (currentValue << pixels) | porchValue;
            int carry = currentValue >>> (8 - pixels) & 0xFF;
            return new ShiftResult((byte) value, (byte) carry);
        }
    }

    /**
     * Operations which can shift data to affect `scroll up`.
     * <ul>
     *     <li>The current value is shifted right by the specified number of pixels.</li>
     *     <li>The carry is returned in the porch value.</li>
     *     <li>The porch value is or-ed with the new value.</li>
     * </ul>
     */
    static class ScrollUpOperation implements ShiftOperation {
        @Override
        public ShiftResult shift(int currentValue, int porchValue, int pixels) {
            int value = (currentValue >>> pixels) | porchValue;
            int carry = currentValue << (8 - pixels) & 0xFF;
            return new ShiftResult((byte) value, (byte) carry);
        }
    }

    /**
     * The result of a shift operation.
     * @param value the result of shifting the current value by the specified number of pixels
     * @param porch the carry bits resulting from the shift operation
     */
    record ShiftResult(byte value, byte porch) {
        ShiftResult(int value, int porch) {
            this((byte) value, (byte) porch);
        }
    }
}
