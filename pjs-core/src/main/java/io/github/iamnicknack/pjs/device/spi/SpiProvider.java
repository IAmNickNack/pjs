package io.github.iamnicknack.pjs.device.spi;

import io.github.iamnicknack.pjs.device.spi.impl.DefaultSpiTransfer;
import io.github.iamnicknack.pjs.model.device.DeviceProvider;

/**
 * Factory for SPI devices.
 */
public interface SpiProvider extends DeviceProvider<Spi, SpiConfig>, SpiTransferProvider {
    @Override
    default void close() {}

    @Override
    default SpiTransfer createTransfer(Spi spi) {
        return new DefaultSpiTransfer(spi);
    }
}
