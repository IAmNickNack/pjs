package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.DeviceProvider;

/**
 * Factory for PWM devices.
 */
public interface PwmProvider extends DeviceProvider<Pwm, PwmConfig> {
    @Override
    default void close() {}
}
