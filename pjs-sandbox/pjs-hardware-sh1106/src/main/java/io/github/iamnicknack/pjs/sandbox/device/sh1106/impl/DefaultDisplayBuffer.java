package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;

/**
 * Default implementation of {@link DisplayOperations} that uses a byte array as the display buffer.
 */
public class DefaultDisplayBuffer implements DisplayOperations {

    private final byte[][] buffer = new byte[PAGE_COUNT][DisplayOperations.PAGE_SIZE];

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        if (column + length > DisplayOperations.PAGE_SIZE) {
            throw new IllegalArgumentException("Requested data is outside the display buffer");
        }
        System.arraycopy(data, offset, this.buffer[page], column, length);
    }

    @Override
    public void clearData(int page, int column, int length) {
        setData(page, column, new byte[length], 0, length);
    }

    @Override
    public void getData(int page, int column, byte[] buffer, int offset, int length) {
        if (column + length > DisplayOperations.PAGE_SIZE) {
            throw new IllegalArgumentException("Requested data is outside the display buffer");
        }
        System.arraycopy(this.buffer[page], column, buffer, offset, length);
    }

    @Override
    public int getPointValue(int page, int column) {
        if (column > DisplayOperations.PAGE_SIZE) {
            throw new IllegalArgumentException("Requested data is outside the display buffer");
        }
        return buffer[page][column] & 0xFF;
    }

    @Override
    public void orData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, (current, operand) -> (byte) (current | operand));
    }

    @Override
    public void xorData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, (current, operand) -> (byte) (current ^ operand));
    }

    @Override
    public void andData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, (current, operand) -> (byte) (current & operand));
    }

    private void modifyData(int page, int column, byte[] data, int offset, int length, ModifyOperator modifier) {
        byte[] pageData = buffer[page];
        for (int i = 0; i < length; i++) {
            int index = (column + i) % PAGE_SIZE;
            pageData[index] = modifier.modify(pageData[index], data[(offset + i)]);
        }
    }

    @FunctionalInterface
    private interface ModifyOperator {
        byte modify(byte current, byte operand);
    }
}
