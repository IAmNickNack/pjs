package io.github.iamnicknack.pjs.sandbox.device.sh1106;

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
//         int clamp = Math.min(length, DisplayOperations.PAGE_SIZE - column);
//         while (clamp < length) {
//             System.arraycopy(data, offset, buffer[page], column, clamp);
//
//             column = 0;
//             offset += clamp;
//             clamp += clamp;
//         }
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
}
