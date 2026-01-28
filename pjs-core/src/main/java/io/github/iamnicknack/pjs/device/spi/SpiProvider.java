package io.github.iamnicknack.pjs.device.spi;

import io.github.iamnicknack.pjs.device.spi.impl.DefaultSpiTransfer;
import io.github.iamnicknack.pjs.model.device.DeviceProvider;

/**
 * Factory for SPI devices.
 */
public interface SpiProvider extends DeviceProvider<Spi, SpiConfig>, SpiTransferProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    default void close() {}

    /**
     * Create a new {@link SpiTransfer}
     * @param spi the SPI to delegate to.
     * @return a transfer instance
     */
    @Override
    default SpiTransfer createTransfer(Spi spi) {
        return new DefaultSpiTransfer(spi);
    }
}
