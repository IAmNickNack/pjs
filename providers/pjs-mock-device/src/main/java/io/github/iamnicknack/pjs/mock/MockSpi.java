package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

import java.nio.ByteBuffer;

public class MockSpi implements Spi {

    private final SpiConfig config;

    private final ByteBuffer inBuffer;
    private final ByteBuffer outBuffer;

    public MockSpi(SpiConfig config, int maxBufferSize) {
        this.config = config;
        this.inBuffer = ByteBuffer.allocate(maxBufferSize);
        this.outBuffer = ByteBuffer.allocate(maxBufferSize);
    }

    public MockSpi(SpiConfig config) {
        this(config, 256);
    }

    @Override
    public DeviceConfig<Spi> getConfig() {
        return config;
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
        if (length > inBuffer.remaining()) {
            throw new IllegalStateException("Read buffer is too large");
        }
        if (length > outBuffer.remaining()) {
            throw new IllegalStateException("Write buffer is too large");
        }

        outBuffer.put(write, writeOffset, length);
        inBuffer.get(read,  readOffset, length);

        return length;
    }

    /**
     * Moves the contents of the output buffer into the input buffer, allowing previously
     * written bytes to be read.
     */
    public void swapBuffers() {
        if (outBuffer.position() > 0) {
            inBuffer.clear();

            var bytes = new byte[outBuffer.limit()];
            outBuffer.get(0, bytes);
            inBuffer.put(0, bytes);

            outBuffer.clear();
        }
    }

    /**
     * @return The buffer used to populate bytes read
     */
    public ByteBuffer getInBuffer() {
        return inBuffer;
    }

    /**
     * @return The buffer used to store written bytes
     */
    public ByteBuffer getOutBuffer() {
        return outBuffer;
    }

    /**
     * Clear both buffers
     */
    public void reset() {
        inBuffer.clear();
        outBuffer.clear();
    }

}
