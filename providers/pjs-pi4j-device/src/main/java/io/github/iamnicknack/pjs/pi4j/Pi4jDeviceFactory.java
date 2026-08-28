package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.GenericDeviceFactoryBuilder;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;

public class Pi4jDeviceFactory implements GenericDeviceFactory {

    private final GenericDeviceFactory factory;

    public Pi4jDeviceFactory(Context pi4j) {
        this.factory = new GenericDeviceFactoryBuilder()
                .factory(new Pi4JPortFactory(pi4j), GpioPortConfig.class)
                .factory(new Pi4JSpiFactory(pi4j), SpiConfig.class)
                .factory(new Pi4JI2CFactory(pi4j), I2CConfig.class)
                .factory(new Pi4JPwmFactory(pi4j), PwmConfig.class)
                .build();
    }

    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        return factory.create(config);
    }
}
