package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.Pi4J;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Pi4jDeviceRegistryTest {

    @Test
    void canRemoveDevices() {
        var context = Pi4J.newContextBuilder()
                .add(new MockDigitalInputProviderImpl())
                .add(new MockDigitalOutputProviderImpl())
                .build();
        var registry = new Pi4jDeviceRegistry(context);

        var device = registry.create(GpioPortConfig.builder().pin(1, 2).build());

        context.registry().all().forEach((s, io) -> System.out.println(s + ", " + io.getClass().getName()));

        assertThat(registry.contains(device.getConfig().getId())).isTrue();
        assertThat(context.registry().all().size()).isEqualTo(2);

        registry.remove(device.getConfig().getId());

        assertThat(registry.contains(device.getConfig().getId())).isFalse();
        assertThat(context.registry().all().size()).isEqualTo(0);
    }


}