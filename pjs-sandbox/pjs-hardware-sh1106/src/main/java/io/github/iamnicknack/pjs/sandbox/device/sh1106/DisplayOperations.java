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
        setPosition(page, 0);
        setData(new byte[PAGE_SIZE], 0, PAGE_SIZE);
    }

    /**
     * Write to the display buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    default void setData(int page, int column, byte[] data, int offset, int length) {
        setPosition(page, column);
        setData(data, offset, length);
    }

    /**
     * Write to the display buffer at the specified position.
     * @param position The position to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    default void setData(int position, byte[] data, int offset, int length) {
        var page = position / PAGE_SIZE;
        var column = position % PAGE_SIZE;
        setData(page, column, data, offset, length);
    }

    /**
     * Write to the display buffer at the current location.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void setData(byte[] data, int offset, int length);

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
     * Set the current display position.
     * @param page The page to set the position on.
     * @param column The column to set the position on.
     */
    void setPosition(int page, int column);

    /**
     * Copy the current display buffer contents into the given display.
     * @param other The display to copy into.
     */
    default void copyTo(DisplayOperations other) {
        throw new UnsupportedOperationException();
    }
}
