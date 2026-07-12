package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.ffm.device.NativeI2CFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePortFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePwmFactory;
import io.github.iamnicknack.pjs.ffm.device.NativeSpiFactory;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

/**
 * A {@link DefaultDeviceRegistry} with native factories registered
 */
public class NativeDeviceRegistry extends DefaultDeviceRegistry {
    NativeDeviceRegistry(
            NativePortFactory nativePortFactory,
            NativeSpiFactory nativeSpiFactory,
            NativePwmFactory nativePwmFactory,
            NativeI2CFactory nativeI2CFactory
    ) {
        this.registerFactory(nativePortFactory, GpioPortConfig.class);
        this.registerFactory(nativeSpiFactory, SpiConfig.class);
        this.registerFactory(nativePwmFactory, PwmConfig.class);
        this.registerFactory(nativeI2CFactory, I2CConfig.class);
    }
}
