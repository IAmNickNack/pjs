package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.spi.Spi;

import java.nio.ByteBuffer;

public interface MockSpi extends Spi {
    /**
     * Moves the contents of the output buffer into the input buffer, allowing previously
     * written bytes to be read.
     */
    void swapBuffers();

    /**
     * @return The buffer used to populate bytes read
     */
    ByteBuffer getInBuffer();

    /**
     * @return The buffer used to store written bytes
     */
    ByteBuffer getOutBuffer();

    /**
     * Clear both buffers
     */
    void reset();
}
