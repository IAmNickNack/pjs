package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CFactory;

public class MockI2CFactory implements I2CFactory {

    private final int maxBufferSize;

    public MockI2CFactory(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public MockI2CFactory() {
        this(256);
    }

    @Override
    public I2C create(I2CConfig config) {
        return new MockI2C(config, maxBufferSize);
    }
}
