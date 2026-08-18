package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry;

/**
 * A {@link DefaultDeviceRegistry} with mock factories registered
 */
public final class MockDeviceRegistry extends DefaultDeviceRegistry {
    public MockDeviceRegistry() {
        this.registerFactory(new MockGpioPortFactory(), GpioPortConfig.class);
        this.registerFactory(new MockSpiFactory(), SpiConfig.class);
        this.registerFactory(new MockPwmFactory(), PwmConfig.class);
        this.registerFactory(new MockI2CFactory(), I2CConfig.class);
    }
}
