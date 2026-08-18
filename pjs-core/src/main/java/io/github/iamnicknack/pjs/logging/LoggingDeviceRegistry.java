package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortFactory;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CFactory;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmFactory;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public final class LoggingDeviceRegistry extends DefaultDeviceRegistry {
    private final DeviceRegistry delegate;

    public LoggingDeviceRegistry(DeviceRegistry delegate) {
        this.delegate = delegate;
        registerFactory((GpioPortFactory) config -> new LoggingGpioPort(delegate.create(config)), GpioPortConfig.class);
        registerFactory((PwmFactory) config -> new LoggingPwm(delegate.create(config)), PwmConfig.class);
        registerFactory((I2CFactory) config -> new LoggingI2C(delegate.create(config)), I2CConfig.class);
        registerFactory(new LoggingSpiFactory(delegate), SpiConfig.class);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
