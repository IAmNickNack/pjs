package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

public class Pi4jDeviceRegistry extends DefaultDeviceRegistry {
    public Pi4jDeviceRegistry(Context pi4jContext) {
        this.registerProvider(new Pi4jPortProvider(pi4jContext), GpioPortConfig.class);
        this.registerProvider(new Pi4jSpiProvider(pi4jContext), SpiConfig.class);
        this.registerProvider(new Pi4jPwmProvider(pi4jContext), PwmConfig.class);
    }
}
