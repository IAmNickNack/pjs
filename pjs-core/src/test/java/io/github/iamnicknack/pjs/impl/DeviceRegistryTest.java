package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DeviceRegistryTest {

    private final GenericDeviceFactory factory = new MockDeviceRegistry();

    @Test
    void canCreateDevice() {
        try (var registry = factory.asDeviceRegistry()) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = registry.create(config);

            assertThat(device.getConfig()).isEqualTo(config);
            assertThat(registry.device(config.id(), GpioPort.class)).isNotNull();
        }
    }

    @Test
    void canRemoveDevice() throws Exception {
        try (var registry = factory.asDeviceRegistry()) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = registry.create(config);

            device.close();

            assertThat(registry.device(config.id(), GpioPort.class)).isNull();
        }
    }

    @Test
    void missingProviderThrowsException() {
        try (var registry = GenericDeviceFactory.builder().build().asDeviceRegistry()) {
            var config = GpioPortConfig.builder().pin(1).build();
            assertThatExceptionOfType(DefaultDeviceRegistry.RegistryException.class)
                    .isThrownBy(() -> registry.create(config))
                    .withCauseInstanceOf(IllegalArgumentException.class);
        }
    }

}