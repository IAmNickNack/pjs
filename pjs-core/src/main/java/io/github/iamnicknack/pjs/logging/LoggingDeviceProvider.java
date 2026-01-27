package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.GenericDeviceProvider;

public class LoggingDeviceProvider implements GenericDeviceProvider {

    private final GenericDeviceProvider delegate;

    public LoggingDeviceProvider(GenericDeviceProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        T device = delegate.create(config);

        switch (device) {
            case GpioPort port -> {
                return (T) new LoggingGpioPort(port);
            }
            case Pwm pwm -> {
                return (T) new LoggingPwm(pwm);
            }
            case I2C i2c -> {
                return (T) new LoggingI2C(i2c);
            }
            case Spi spi -> {
                return (T) new LoggingSpi(spi);
            }
            default -> throw new DefaultDeviceRegistry.RegistryException("Unsupported device type for logging: " + device.getClass(), new IllegalArgumentException());
        }
    }
}
