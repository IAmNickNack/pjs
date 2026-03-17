package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.VerticalScrollOperations;

/**
 * A display buffer that uses a long array to store pixel data.
 * Each long represents an entire column of the display, with 64-bits per column.
 */
public class LongDisplayBuffer implements BufferedDisplayOperations {

    private final long[] buffer = new long[PAGE_SIZE];

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        var pageMask = ~(0xffL << (page * 8));
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            this.buffer[index] &= pageMask;
            this.buffer[index] |= (data[offset + i] & 0xffL) << (page * 8);
        }
    }

    @Override
    public void orData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            this.buffer[index] |= (data[offset + i] & 0xffL) << (page * 8);
        }
    }

    @Override
    public void andData(int page, int column, byte[] data, int offset, int length) {
        var pageMask = ~(0xffL << (page * 8));
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            var mask = (data[offset + i] & 0xffL) << (page * 8) | pageMask;
            this.buffer[index] &= mask;
        }
    }

    @Override
    public void xorData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            this.buffer[index] ^= (data[offset + i] & 0xffL) << (page * 8);
        }
    }

    @Override
    public void andNotData(int page, int column, byte[] data, int offset, int length) {
        var pageMask = ~(0xffL << (page * 8));
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            var mask = (data[offset + i] & 0xffL) << (page * 8) | pageMask;
            this.buffer[index] &= ~mask;
        }
    }

    @Override
    public void getData(int page, int column, byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            var index = (column + i) % PAGE_SIZE;
            buffer[offset + i] = (byte) (this.buffer[index] >> (page * 8));
        }
    }

    @Override
    public int getPointValue(int page, int column) {
        return (int) (this.buffer[(column) % PAGE_SIZE] >> (page * 8)) & 0xff;
    }

    @Override
    public void copyTo(DisplayOperations other) {
        for (int page = 0; page < PAGE_COUNT; page++) {
            var pageData = new byte[PAGE_SIZE];
            getData(page, 0, pageData, 0, PAGE_SIZE);
            other.setData(page, 0, pageData, 0, PAGE_SIZE);
        }
    }

    public long getColumn(int column) {
        return this.buffer[column % PAGE_SIZE];
    }

    public void setColumn(int column, long value) {
        this.buffer[column % PAGE_SIZE] = value;
    }

    public VerticalScrollOperations scrollUpOperations() {
        return (pixels, initialPorch) -> {
            byte[] porch = new byte[PAGE_SIZE];
            System.arraycopy(initialPorch, 0, porch, 0, PAGE_SIZE);
            for (int i = 0; i < PAGE_SIZE; i++) {
                long value = LongDisplayBuffer.this.buffer[i];
                value >>>= pixels;
                value |= porch[i];
                porch[i] = (byte)(LongDisplayBuffer.this.buffer[i] << (8 - pixels));
                LongDisplayBuffer.this.buffer[i] = value;
            }
            return porch;
        };
    }

    public VerticalScrollOperations scrollDownOperations() {
        return (pixels, initialPorch) -> {
            byte[] porch = new byte[PAGE_SIZE];
            System.arraycopy(initialPorch, 0, porch, 0, PAGE_SIZE);
            for (int i = 0; i < PAGE_SIZE; i++) {
                long value = LongDisplayBuffer.this.buffer[i];
                value <<= pixels;
                value |= porch[i];
                porch[i] = (byte)(LongDisplayBuffer.this.buffer[i] >>> -pixels);
                LongDisplayBuffer.this.buffer[i] = value;
            }
            return porch;
        };
    }

    public VerticalScrollOperations rotateUpOperations() {
        return (pixels, initialPorch) -> {
            byte[] porch = new byte[PAGE_SIZE];
            System.arraycopy(initialPorch, 0, porch, 0, PAGE_SIZE);
            for (int i = 0; i < PAGE_SIZE; i++) {
                long value = Long.rotateRight(LongDisplayBuffer.this.buffer[i], pixels);
                porch[i] = (byte)(value >>> 56);
                LongDisplayBuffer.this.buffer[i] = value;
            }
            return porch;
        };
    }

    public VerticalScrollOperations rotateDownOperations() {
        return (pixels, initialPorch) -> {
            byte[] porch = new byte[PAGE_SIZE];
            System.arraycopy(initialPorch, 0, porch, 0, PAGE_SIZE);
            for (int i = 0; i < PAGE_SIZE; i++) {
                long value = Long.rotateLeft(LongDisplayBuffer.this.buffer[i], pixels);
                porch[i] = (byte)value;
                LongDisplayBuffer.this.buffer[i] = value;
            }
            return porch;
        };
    }}
