package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.DeviceProvider;

/**
 * Factory interface for creating {@link GpioPort} instances.
 */
public interface GpioPortProvider extends DeviceProvider<GpioPort, GpioPortConfig> {
    /**
     * {@inheritDoc}
     */
    default void close() {}
}
