package io.github.iamnicknack.pjs.sandbox.device.sh1106;

public interface DisplayOperations {

    int PAGE_SIZE = 128;
    int PAGE_COUNT = 8;
    int BUFFER_SIZE = PAGE_SIZE * PAGE_COUNT;
    int BLOCK_SIZE = 16;
    int BLOCK_COUNT = BUFFER_SIZE / BLOCK_SIZE;

    /**
     * Clear the entire display.
     */
    default void clear() {
        for (int i = 0; i < 8; i++) {
            clearPage(i);
        }
    }
    /**
     * Clear a single page of the display.
     */
    default void clearPage(int page) {
        setData(page, 0, new byte[PAGE_SIZE], 0, PAGE_SIZE);
    }

    /**
     * Write to the display buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void setData(int page, int column, byte[] data, int offset, int length);

    /**
     * `or` the provided data with the existing data in the buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void orData(int page, int column, byte[] data, int offset, int length);

    /**
     * `and` the provided data with the existing data in the buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void andData(int page, int column, byte[] data, int offset, int length);

    /**
     * `xor` the provided data with the existing data in the buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void xorData(int page, int column, byte[] data, int offset, int length);

    /**
     * Clear a section of the display buffer at the specified location.
     * @param page The page to clear.
     * @param column The column to start clearing from.
     * @param length The number of bytes to clear.
     */
    default void clearData(int page, int column, int length) {
        setData(page, column, new byte[length], 0, length);
    }

    /**
     * Read data from the display buffer at the specified location.
     * @param page The page to read from.
     * @param column The column to read from.
     * @param buffer The buffer to read into.
     * @param offset The offset within the buffer to start writing to.
     * @param length The number of bytes to read.
     */
    void getData(int page, int column, byte[] buffer, int offset, int length);

    /**
     * Get the value of a point on the display.
     * @param page The page of the point.
     * @param column The column of the point.
     * @return The data value at the point.
     */
    int getPointValue(int page, int column);

    /**
     * Copy the current display buffer contents into the given display.
     * @param other The display to copy into.
     */
    default void copyTo(DisplayOperations other) {
        throw new UnsupportedOperationException();
    }

    /**
     * Represents a functional interface for performing operations on display data.
     * This interface allows users to define custom data manipulation logic, which
     * can then be applied to the display buffer at specified positions.
     *
     * Implementations of this interface can provide functionality such as modifying,
     * clearing, or combining data in specific regions of the display buffer.
     *
     * This interface is primarily used to define data operations dynamically.
     */
    @FunctionalInterface
    interface DataOperation {
        void apply(int page, int column, byte[] data, int offset, int length);
    }
}
