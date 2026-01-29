package io.github.iamnicknack.pjs.model;

import java.io.InputStream;

/**
 * A read operation for serial data.
 * <p>
 * The operation can be used to read multiple bytes from a device.
 * </p>
 */
public interface SerialReadOperation {

    /**
     * Read `length` bytes into `buffer` starting at `offset`
     * @param buffer the buffer to fill
     * @param offset the offset at which to start setting bytes
     * @param length the number of bytes to read
     * @return the number of bytes read
     */
    int readBytes(byte[] buffer, int offset, int length);

    /**
     * Read into the given buffer
     * @param buffer The buffer to read into.
     * @return The number of bytes read.
     */
    default int readBytes(byte[] buffer) {
        return readBytes(buffer, 0, buffer.length);
    }

    /**
     * Default single-byte read operation
     * @return the byte read
     */
    default byte readByte() {
        byte[]  buffer = new byte[1];
        readBytes(buffer);
        return buffer[0];
    }

    /**
     * Read `len` bytes
     * @param len the number of bytes to read
     * @return bytes read
     */
    default byte[] readBytes(int len) {
        byte[] buffer = new byte[len];
        readBytes(buffer, 0, len);
        return buffer;
    }

    /**
     * Get an input stream that can be used to read from the device.
     * @return An input stream.
     */
    default InputStream getInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return SerialReadOperation.this.readByte() & 0xff;
            }

            @Override
            public int read(byte[] buffer) {
                return SerialReadOperation.this.readBytes(buffer);
            }

            @Override
            public int read(byte[] buffer, int off, int len) {
                return SerialReadOperation.this.readBytes(buffer, off, len);
            }
        };
    }

}
