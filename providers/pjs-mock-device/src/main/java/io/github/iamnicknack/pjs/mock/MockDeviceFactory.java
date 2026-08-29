package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;

public class MockDeviceFactory implements GenericDeviceFactory {

    private final GenericDeviceFactory factory = GenericDeviceFactory.builder()
            .factory(new MockGpioPortFactory(), GpioPortConfig.class)
            .factory(new MockI2CFactory(), I2CConfig.class)
            .factory(new MockSpiFactory(), SpiConfig.class)
            .factory(new MockPwmFactory(), PwmConfig.class)
            .build();

    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        return factory.create(config);
    }
}
