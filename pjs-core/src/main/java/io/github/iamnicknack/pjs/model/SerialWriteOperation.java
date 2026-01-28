package io.github.iamnicknack.pjs.model;

import java.io.OutputStream;

/**
 * A write operation for serial data.
 * <p>
 * The operation can be used to write multiple bytes to a device.
 * </p>
 */
public interface SerialWriteOperation {

    /**
     * Write `length` bytes from `buffer` starting at `offset`
     * @param buffer the data to write
     * @param offset the offset of the first byte to send
     * @param length the number of bytes to send
     */
    void writeBytes(byte[] buffer, int offset, int length);

    /**
     * Write the given buffer to the device.
     * @param buffer The buffer to write.
     */
    default void writeBytes(byte[] buffer) {
        writeBytes(buffer, 0, buffer.length);
    }

    /**
     * Default single-byte write operation
     * @param b the byte to write
     */
    default void writeByte(byte b) {
        writeBytes(new byte[] { b }, 0, 1);
    }

    /**
     * Get an output stream that can be used to write to the device.
     * @return An output stream.
     */
    default OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int value) {
                SerialWriteOperation.this.writeByte((byte)value);
            }

            @Override
            public void write(byte[] data) {
                SerialWriteOperation.this.writeBytes(data);
            }

            @Override
            public void write(byte[] data, int off, int len) {
                SerialWriteOperation.this.writeBytes(data, off, len);
            }
        };
    }
}
