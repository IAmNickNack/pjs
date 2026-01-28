package io.github.iamnicknack.pjs.device.spi;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.port.Port;
import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * SPI contract for devices implementing the SPI protocol.
 */
public interface Spi extends SerialPort, Port<Integer>, Device<Spi> {

    /**
     * Perform a single SPI transfer.
     * @param write the data to write.
     * @param writeOffset the offset in the write buffer.
     * @param read the data to read.
     * @param readOffset the offset in the read buffer.
     * @param length the number of bytes to transfer.
     * @return the number of bytes transferred.
     */
    int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length);

    /**
     * Perform an SPI transfer.
     * @param write the data to write.
     * @param read the data to read.
     * @return the number of bytes read.
     */
    default int transfer(byte[] write, byte[] read) {
        int length = Math.min(write.length, read.length);
        return transfer(write, 0, read, 0, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void writeBytes(byte[] buffer, int offset, int length) {
        transfer(buffer, offset, new byte[length], 0, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default int readBytes(byte[] buffer, int offset, int length) {
        return transfer(new byte[length], offset, buffer, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void write(Integer value) {
        writeBytes(new byte[] { value.byteValue() }, 0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer read() {
        byte[] buffer = new byte[1];
        readBytes(buffer);
        return (int)buffer[0];
    }
}
