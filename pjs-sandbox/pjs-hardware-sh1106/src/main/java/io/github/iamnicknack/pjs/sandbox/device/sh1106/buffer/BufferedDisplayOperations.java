package io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;

import java.util.ArrayList;
import java.util.List;

public interface BufferedDisplayOperations extends DisplayOperations, CopySource {

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
     * `andNot` the provided data with the existing data in the buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void andNotData(int page, int column, byte[] data, int offset, int length);

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
     * Create a {@link CopySource} that can be used to transfer the current display buffer contents.
     * @return A {@link CopySource} that can be used to restore the display buffer contents.
     */
    default CopySource snapshot() {
        List<CopySource> operations = new ArrayList<>();
        this.copyTo(
                (page, column, data, offset, length) ->
                        operations.add(displayOperations ->
                                displayOperations.setData(page, column, data, offset, length)
                        )
        );

        return other -> operations.forEach(operation -> operation.copyTo(other));
    }

    /**
     * Copy the current display buffer contents from the given displays.
     * @param others The displays to copy from.
     */
    default void copyFrom(CopySource...others) {
        for (var other : others) {
            other.copyTo(this);
        }
    }
}
