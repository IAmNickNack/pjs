package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class LoggingSpiProvider implements SpiProvider {
    private final DeviceRegistry deviceRegistry;
    private final SpiProvider spiProviderDelegate;

    public LoggingSpiProvider(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.spiProviderDelegate = (SpiProvider) deviceRegistry.getProvider(SpiConfig.class);
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
