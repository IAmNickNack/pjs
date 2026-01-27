package io.github.iamnicknack.pjs.device.i2c.impl;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * An I2C register implementation that provides read and write operations for a single register on an I2C device.
 */
public class I2CRegister implements SerialPort {
    private final int address;
    private final int register;
    private final I2C delegate;

    /**
     * Constructs an I2C register with the specified address, register number, and I2C delegate.
     * @param address The I2C address of the device.
     * @param register The register number within the device.
     * @param delegate The I2C delegate for communication.
     */
    public I2CRegister(int address, int register, I2C delegate) {
        this.address = address;
        this.register = register;
        this.delegate = delegate;
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(
                I2C.Message.write(address, new byte[] { (byte)register }, 0, 1),
                I2C.Message.read(address, buffer, offset, length)
        );
        return length;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(
                I2C.Message.write(address, new byte[] { (byte)register }, 0, 1),
                I2C.Message.write(address, buffer, offset, length)
        );
    }

    @Override
    public Integer read() {
        var buffer = new byte[1];
        readBytes(buffer, 0, 1);
        return buffer[0] & 0xFF;
    }

    @Override
    public void write(Integer value) {
        var buffer = new byte[] { value.byteValue() };
        writeBytes(buffer, 0, 1);
    }
}
