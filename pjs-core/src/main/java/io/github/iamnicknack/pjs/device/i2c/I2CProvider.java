package io.github.iamnicknack.pjs.device.i2c;

import io.github.iamnicknack.pjs.model.device.DeviceProvider;

/**
 * Factory for I2C devices.
 */
public interface I2CProvider extends DeviceProvider<I2C, I2CConfig> {
    /**
     * {@inheritDoc}
     */
    @Override
    default void close() {}
}
