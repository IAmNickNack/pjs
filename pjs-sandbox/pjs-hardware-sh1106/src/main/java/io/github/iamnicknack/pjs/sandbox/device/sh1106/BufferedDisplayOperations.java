package io.github.iamnicknack.pjs.sandbox.device.sh1106;

public interface BufferedDisplayOperations extends DisplayOperations {

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
}
