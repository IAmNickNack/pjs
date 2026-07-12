package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.DeviceFactory;

/**
 * Factory interface for creating {@link GpioPort} instances.
 */
public interface GpioPortFactory extends DeviceFactory<GpioPort, GpioPortConfig> {
    /**
     * {@inheritDoc}
     */
    default void close() {}
}
