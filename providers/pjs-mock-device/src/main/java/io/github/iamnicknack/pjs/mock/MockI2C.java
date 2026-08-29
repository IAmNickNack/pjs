package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.i2c.I2C;

import java.nio.ByteBuffer;

public interface MockI2C extends I2C {
    /**
     * Access the device buffer
     * @return the device buffer
     */
    ByteBuffer getDeviceBuffer();

    /**
     * Access the buffer associated with a register
     * @param register the register address
     * @return the buffer for that register
     */
    ByteBuffer getBuffer(int register);

    /**
     * Reset the device and register buffers.
     * - the device buffer is cleared
     * - all register buffers are discarded
     */
    void reset();
}
