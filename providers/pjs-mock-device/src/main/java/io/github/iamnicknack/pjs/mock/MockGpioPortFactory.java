package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;

public class MockGpioPortFactory implements DeviceFactory<GpioPort, GpioPortConfig> {
    @Override
    public GpioPort create(GpioPortConfig config) {
        return new MockGpioPort(config);
    }
}
