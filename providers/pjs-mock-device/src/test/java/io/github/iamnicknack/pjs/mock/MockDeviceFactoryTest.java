package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockDeviceFactoryTest {

    private final MockDeviceFactory mockDeviceFactory = new MockDeviceFactory();

    @Test
    void deviceIsRemovedWhenClosed() throws Exception {
        try (var registry = mockDeviceFactory.asDeviceRegistry()) {
            var device = registry.create(GpioPortConfig.builder().id("test").pin(1).build());
            assertTrue(registry.contains(device.getConfig().getId()));
            device.close();
            assertFalse(registry.contains(device.getConfig().getId()));
        }
    }

}