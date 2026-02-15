package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.ffm.device.NativeI2CProvider;
import io.github.iamnicknack.pjs.ffm.device.NativePortProvider;
import io.github.iamnicknack.pjs.ffm.device.NativePwmProvider;
import io.github.iamnicknack.pjs.ffm.device.NativeSpiProvider;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

/**
 * A {@link DefaultDeviceRegistry} with native providers registered
 */
public class NativeDeviceRegistry extends DefaultDeviceRegistry {
    NativeDeviceRegistry(
            NativePortProvider nativePortProvider,
            NativeSpiProvider nativeSpiProvider,
            NativePwmProvider nativePwmProvider,
            NativeI2CProvider nativeI2CProvider
    ) {
        this.registerProvider(nativePortProvider, GpioPortConfig.class);
        this.registerProvider(nativeSpiProvider, SpiConfig.class);
        this.registerProvider(nativePwmProvider, PwmConfig.class);
        this.registerProvider(nativeI2CProvider, I2CConfig.class);
    }
}
