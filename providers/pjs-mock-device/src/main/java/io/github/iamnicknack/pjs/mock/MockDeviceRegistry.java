package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

/**
 * A {@link DefaultDeviceRegistry} with mock providers registered
 */
public class MockDeviceRegistry extends DefaultDeviceRegistry {
    public MockDeviceRegistry() {
        this.registerProvider(new MockGpioPortProvider(), GpioPortConfig.class);
        this.registerProvider(new MockSpiProvider(), SpiConfig.class);
        this.registerProvider(new MockPwmProvider(), PwmConfig.class);
        this.registerProvider(new MockI2CProvider(), I2CConfig.class);
    }
}
