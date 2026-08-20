package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiFactory;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class LoggingSpiFactory implements SpiFactory {
    private final DeviceRegistry deviceRegistry;

    public LoggingSpiFactory(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public Spi create(SpiConfig config) {
        return new LoggingSpi(deviceRegistry.create(config));
    }
}
