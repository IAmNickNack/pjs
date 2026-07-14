package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiFactory;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class LoggingSpiFactory implements SpiFactory {
    private final DeviceRegistry deviceRegistry;
    private final SpiFactory spiProviderDelegate;

    public LoggingSpiFactory(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.spiProviderDelegate = (SpiFactory) deviceRegistry.getFactory(SpiConfig.class);
    }

    @Override
    public Spi create(SpiConfig config) {
        return new LoggingSpi(deviceRegistry.create(config));
    }

    @Override
    public SpiTransfer createTransfer(Spi spi) {
        return new LoggingSpiTransfer(spiProviderDelegate.createTransfer(spi), spi.getConfig().getId());
    }
}
