package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;

/**
 * An implementation of {@link BufferedDisplayOperations} that allows pixels to be stacked on top of each other.
 * <p>
 * This can allow layers of data to be added and removed from a display buffer, seemingly independently of each other.
 */
public class StackedDisplayBuffer implements BufferedDisplayOperations {

    private final PixelGroup[][] pixelGroups = new PixelGroup[PAGE_COUNT][PAGE_SIZE];

    public StackedDisplayBuffer() {
        for (int i = 0; i < pixelGroups.length; i++) {
            pixelGroups[i] = new PixelGroup[PAGE_SIZE];
            for (int j = 0; j < pixelGroups[i].length; j++) {
                pixelGroups[i][j] = new PixelGroup();
            }
        }
    }

    @Override
    public void clearPage(int page) {
        for (int i = 0; i < PAGE_SIZE; i++) {
            pixelGroups[page][i].setValue((byte)0);
        }
    }

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            pixelGroups[page][(column + i) % PAGE_SIZE].setValue(data[offset + i]);
        }
    }

    @Override
    public void orData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            pixelGroups[page][(column + i) % PAGE_SIZE].addValue(data[offset + i]);
        }
    }

    @Override
    public void andData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            var group = pixelGroups[page][(column + i) % PAGE_SIZE];
            group.setValue((byte) (group.getValue() & data[offset + i]));
        }
    }

    @Override
    public void xorData(int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            var group = pixelGroups[page][(column + i) % PAGE_SIZE];
            group.setValue((byte) (group.getValue() ^ data[offset + i]));
        }
    }

    @Override
    public void getData(int page, int column, byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer[offset + i] = pixelGroups[page][(column + i) % PAGE_SIZE].getValue();
        }
    }

    @Override
    public int getPointValue(int page, int column) {
        return pixelGroups[page][column % PAGE_SIZE].getValue() & 0xFF;
    }

    @Override
    public void copyTo(DisplayOperations other) {
        BufferedDisplayOperations.super.copyTo(other);
    }

//    /**
//     * Set data in additive mode where pixels are "stacked" on top of each other by
//     * adding `1` to the count of values set on each pixel.
//     *
//     * @param page The page to set data on.
//     * @param column The column to start setting data from.
//     * @param data The data to set.
//     * @param offset The offset into the data array.
//     * @param length The number of pixels to set.
//     */
//    public void addData(int page, int column, byte[] data, int offset, int length) {
//        for (int i = 0; i < length; i++) {
//            pixelGroups[page][(column + i) % PAGE_SIZE].addValue(data[offset + i]);
//        }
//    }

    /**
     * Set data in subtractive mode where pixels are "subtracted" from the count set
     * for the pixel
     *
     * @param page The page to set data on.
     * @param column The column to start setting data from.
     * @param data The data to be subtracted from the pixel count.
     * @param offset The offset into the data array.
     * @param length The number of pixels to set.
     */
    public void subtractValue (int page, int column, byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++) {
            pixelGroups[page][(column + i) % PAGE_SIZE].subtractValue(data[offset + i]);
        }
    }

    PixelGroup getPixelGroup(int page, int column) {
        return pixelGroups[page][column % PAGE_SIZE];
    }

    /**
     * Return a DisplayOperations instance that applies additive operations to the buffer.
     * @return A DisplayOperations instance for additive operations.
     */
    public BufferedDisplayOperations additive() {
        return new DerivedDisplayOperations() {
            @Override
            public void setData(int page, int column, byte[] data, int offset, int length) {
                StackedDisplayBuffer.this.orData(page, column, data, offset, length);
            }
        };
    }

    /**
     * Return a DisplayOperations instance that applies subtractive operations to the buffer.
     * @return A DisplayOperations instance for subtractive operations.
     */
    public BufferedDisplayOperations subtractive() {
        return new DerivedDisplayOperations() {
            @Override
            public void setData(int page, int column, byte[] data, int offset, int length) {
                StackedDisplayBuffer.this.subtractValue(page, column, data, offset, length);
            }

            @Override
            public void orData(int page, int column, byte[] data, int offset, int length) {
                StackedDisplayBuffer.this.subtractValue(page, column, data, offset, length);
            }
        };
    }

    /**
     * Base class for display operations derived from this instance
     */
    abstract class DerivedDisplayOperations implements BufferedDisplayOperations {
        @Override
        public void clearPage(int page) {
            BufferedDisplayOperations.super.clearPage(page);
        }

        @Override
        public void getData(int page, int column, byte[] buffer, int offset, int length) {
            StackedDisplayBuffer.this.getData(page, column, buffer, offset, length);
        }

        @Override
        public void andData(int page, int column, byte[] data, int offset, int length) {
            StackedDisplayBuffer.this.andData(page, column, data, offset, length);
        }

        @Override
        public void orData(int page, int column, byte[] data, int offset, int length) {
            StackedDisplayBuffer.this.orData(page, column, data, offset, length);
        }

        @Override
        public void xorData(int page, int column, byte[] data, int offset, int length) {
            StackedDisplayBuffer.this.xorData(page, column, data, offset, length);
        }

        @Override
        public int getPointValue(int page, int column) {
            return StackedDisplayBuffer.this.getPointValue(page, column);
        }

        @Override
        public void copyTo(DisplayOperations other) {
            StackedDisplayBuffer.this.copyTo(other);
        }
    }

    /**
     * Represents a single byte value in the display buffer as individual pixels.
     */
    static class PixelGroup {

        private final int[] pixels;

        PixelGroup() {
            this.pixels = new int[8];
        }

        public void addValue(byte value) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] += (value & (1 << i)) != 0 ? 1 : 0;
            }
        }

        public void subtractValue(byte value) {
            for (int i = 0; i < pixels.length; i++) {
                int bit = value & (1 << i);
                if (bit == 0) {
                    continue;
                }
                if (pixels[i] == 0) {
                    throw new IllegalStateException("Pixel value cannot be negative");
                }
                pixels[i] -= 1;
            }
        }

        public void setValue(byte value) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (value & (1 << i)) != 0 ? 1 : 0;
            }
        }

        public byte getValue() {
            byte result = 0;
            for (int i = 0; i < pixels.length; i++) {
                result |= pixels[i] > 0 ? (byte) (1 << i) : 0;
            }
            return result;
        }

        int getPlotCount(int pixelIndex) {
            return pixels[pixelIndex];
        }
    }
}
