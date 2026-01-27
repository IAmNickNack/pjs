package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.*;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

/**
 * A {@link DefaultDeviceRegistry} with native providers registered
 */
public class NativeDeviceRegistry extends DefaultDeviceRegistry {
    public NativeDeviceRegistry(NativeContext context) {
        this.registerProvider(new NativePortProvider((context)), GpioPortConfig.class);
        this.registerProvider(new NativeSpiProvider((context)), SpiConfig.class);
        this.registerProvider(new NativePwmProvider((context)), PwmConfig.class);
        this.registerProvider(new NativeI2CProvider((context)), I2CConfig.class);
    }
}
