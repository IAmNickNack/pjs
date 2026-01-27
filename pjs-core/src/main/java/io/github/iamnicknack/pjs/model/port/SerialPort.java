package io.github.iamnicknack.pjs.model.port;

import io.github.iamnicknack.pjs.model.SerialReadOperation;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;

/**
 * A serial port can accept or produce multiple values
 */
public interface SerialPort extends SerialWriteOperation, SerialReadOperation, Port<Integer> {

    /**
     * Read a single byte from the port.
     * @return the byte read.
     */
    @Override
    default Integer read() {
        return (int)readByte();
    }

    /**
     * Write a single byte to the port.
     * @param value the byte to write.
     */
    @Override
    default void write(Integer value) {
        writeByte(value.byteValue());
    }

    /**
     * Enforce read-only access to the port
     * @return a read-only port
     */
    default SerialPort input() {
        return new SerialPort() {
            @Override
            public int readBytes(byte[] buffer, int offset, int length) {
                return SerialPort.this.readBytes(buffer, offset, length);
            }

            @Override
            public void writeBytes(byte[] buffer, int offset, int length) {
                throw new UnsupportedOperationException("Port is input only");
            }
        };
    }

    /**
     * Enforce write-only access to the port
     * @return a write-only port
     */
    default SerialPort output() {
        return new SerialPort() {
            @Override
            public int readBytes(byte[] buffer, int offset, int length) {
                throw new UnsupportedOperationException("Port is output only");
            }

            @Override
            public void writeBytes(byte[] buffer, int offset, int length) {
                SerialPort.this.writeBytes(buffer, offset, length);
            }
        };
    }
}
