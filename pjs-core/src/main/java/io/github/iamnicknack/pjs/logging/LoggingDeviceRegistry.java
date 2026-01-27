package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CProvider;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmProvider;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class LoggingDeviceRegistry extends DefaultDeviceRegistry {
    private final DeviceRegistry delegate;

    public LoggingDeviceRegistry(DeviceRegistry delegate) {
        this.delegate = delegate;
        registerProvider((GpioPortProvider) config -> new LoggingGpioPort(delegate.create(config)), GpioPortConfig.class);
        registerProvider((PwmProvider) config -> new LoggingPwm(delegate.create(config)), PwmConfig.class);
        registerProvider((I2CProvider) config -> new LoggingI2C(delegate.create(config)), I2CConfig.class);
        registerProvider(new LoggingSpiProvider(delegate), SpiConfig.class);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
