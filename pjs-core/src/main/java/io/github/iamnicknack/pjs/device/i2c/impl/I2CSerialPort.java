package io.github.iamnicknack.pjs.device.i2c.impl;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * A serial port implementation for I2C devices.
 */
public class I2CSerialPort implements SerialPort {

    private final int address;
    private final I2C delegate;

    /**
     * Creates a serial port for the given I2C device.
     * @param address the I2C address of the device.
     * @param delegate the I2C delegate for communication.
     */
    public I2CSerialPort(int address, I2C delegate) {
        this.address = address;
        this.delegate = delegate;
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(I2C.Message.read(address, buffer, offset, length));
        return length;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(I2C.Message.write(address, buffer, offset, length));
    }

    @Override
    public Integer read() {
        var buffer = new byte[1];
        delegate.transfer(I2C.Message.read(address, buffer, 0, buffer.length));
        return buffer[0] & 0xFF;
    }

    @Override
    public void write(Integer value) {
        var buffer = new byte[] { value.byteValue() };
        delegate.transfer(I2C.Message.write(address, buffer, 0, buffer.length));
    }
}
