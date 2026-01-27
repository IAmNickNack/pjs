package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CProvider;

public class MockI2CProvider implements I2CProvider {

    private final int maxBufferSize;

    public MockI2CProvider(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public MockI2CProvider() {
        this(256);
    }

    @Override
    public I2C create(I2CConfig config) {
        return new MockI2C(config, maxBufferSize);
    }
}
