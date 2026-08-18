package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

public final class Pi4jDeviceRegistry extends DefaultDeviceRegistry {
    public Pi4jDeviceRegistry(Context pi4jContext) {
        this.registerFactory(new Pi4JPortFactory(pi4jContext), GpioPortConfig.class);
        this.registerFactory(new Pi4JSpiFactory(pi4jContext), SpiConfig.class);
        this.registerFactory(new Pi4JPwmFactory(pi4jContext), PwmConfig.class);
        this.registerFactory(new Pi4JI2CFactory(pi4jContext), I2CConfig.class);
    }
}
