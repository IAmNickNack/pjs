package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.mock.MockGpioPortProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DeviceRegistryTest {

    @Test
    void canCreateDevice() {
        try (var registry = new DefaultDeviceRegistry()
                .registerProvider(new MockGpioPortProvider(), GpioPortConfig.class)
        ) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = registry.create(config);

            assertThat(device.getConfig()).isEqualTo(config);
            assertThat(registry.device(config.id(), GpioPort.class)).isNotNull();
        }
    }

    @Test
    void canRemoveDevice() {
        try (var registry = new DefaultDeviceRegistry()
                .registerProvider(new MockGpioPortProvider(), GpioPortConfig.class)
        ) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = registry.create(config);

            registry.remove(device);

            assertThat(registry.device(config.id(), GpioPort.class)).isNull();
        }
    }

    @Test
    void missingProviderThrowsException() {
        try (var registry = new DefaultDeviceRegistry()) {
            var config = GpioPortConfig.builder().pin(1).build();
            assertThatExceptionOfType(DefaultDeviceRegistry.RegistryException.class)
                    .isThrownBy(() -> registry.create(config))
                    .withCauseInstanceOf(IllegalArgumentException.class);
        }
    }

}