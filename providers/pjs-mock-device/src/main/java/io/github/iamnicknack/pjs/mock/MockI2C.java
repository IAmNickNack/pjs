package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

import java.nio.ByteBuffer;
import java.util.Map;

public class MockI2C implements I2C {

    private final I2CConfig config;
    private final Map<Integer, ByteBuffer> registers = new java.util.HashMap<>();
    private final int maxBufferSize;
    private final ByteBuffer deviceBuffer;

    public MockI2C(I2CConfig config, int maxBufferSize) {
        this.config = config;
        this.maxBufferSize = maxBufferSize;
        this.deviceBuffer = ByteBuffer.allocate(maxBufferSize);
    }

    @Override
    public DeviceConfig<I2C> getConfig() {
        return config;
    }

    @Override
    public void transfer(Message[] messages) {
        if (messages.length == 1 && messages[0].type() == Message.Type.READ) {
            readFromBuffer(deviceBuffer, messages[0].data(), messages[0].offset(), messages[0].length());
        }
        else if (messages.length == 1 && messages[0].type() == Message.Type.WRITE) {
            writeToBuffer(deviceBuffer, messages[0].data(), messages[0].offset(), messages[0].length());
        }
        else if (messages.length == 2 &&
                messages[0].type() == Message.Type.WRITE &&
                messages[0].length() == 1) {

            var offset = Integer.valueOf(messages[0].data()[0]);
            var buffer = getBuffer(offset);

            if (messages[1].type() == Message.Type.READ) {
                readFromBuffer(buffer, messages[1].data(), messages[1].offset(), messages[1].length());
            }
            else if (messages[1].type() == Message.Type.WRITE) {
                writeToBuffer(buffer, messages[1].data(), messages[1].offset(), messages[1].length());
            }
        }
        else {
            throw new IllegalArgumentException("Cannot mock messages");
        }
    }

    /**
     * Access the device buffer
     * @return the device buffer
     */
    public ByteBuffer getDeviceBuffer() {
        return deviceBuffer;
    }

    /**
     * Access the buffer associated with a register
     * @param register the register address
     * @return the buffer for that register
     */
    public ByteBuffer getBuffer(int register) {
        return registers.computeIfAbsent(register, _ -> ByteBuffer.allocate(this.maxBufferSize));
    }

    /**
     * Reset the device and register buffers.
     * - the device buffer is cleared
     * - all register buffers are discarded
     */
    public void reset() {
        deviceBuffer.clear();
        registers.clear();
    }


    /**
     * Read from a ByteBuffer into a byte array
     * @param source the source buffer
     * @param dest the destination array
     * @param offset the offset to start reading from
     * @param length the maximum length to read
     * @return the number of bytes read
     */
    private int readFromBuffer(ByteBuffer source, byte[] dest, int offset, int length) {
        int bytesToRead = Math.min(length, source.remaining());
        source.get(dest, offset, bytesToRead);
        return bytesToRead;
    }


    /**
     * Write from a byte array into a ByteBuffer
     * @param dest the destination buffer
     * @param source the source array
     * @param offset the offset to start writing from
     * @param length the maximum length to write
     */
    private void writeToBuffer(ByteBuffer dest, byte[] source, int offset, int length) {
        int bytesToWrite = Math.min(length, dest.remaining());
        dest.put(source, offset, bytesToWrite);
    }
}
