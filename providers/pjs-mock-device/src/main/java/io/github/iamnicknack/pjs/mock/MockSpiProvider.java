package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;

public class MockSpiProvider implements SpiProvider {

    @Override
    public Spi create(SpiConfig config) {
        return new MockSpi(config);
    }
}
