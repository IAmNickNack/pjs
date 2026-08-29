package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.Pi4J;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl;
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Pi4jDeviceRegistryTest {

    @Test
    void canRemoveDigitalDevices() throws Exception {
        var context = Pi4J.newContextBuilder()
                .add(new MockDigitalInputProviderImpl())
                .add(new MockDigitalOutputProviderImpl())
                .build();
        var registry = new Pi4jDeviceFactory(context).asDeviceRegistry();

        var device = registry.create(GpioPortConfig.builder().pin(1, 2).build());

        assertThat(registry.contains(device.getConfig().getId())).isTrue();
        assertThat(context.registry().all().size()).isEqualTo(2);

        device.close();

        assertThat(registry.contains(device.getConfig().getId())).isFalse();
        assertThat(context.registry().all().size()).isEqualTo(0);
    }

    @Test
    void canRemoveI2CDevices() throws Exception {
        var context = Pi4J.newContextBuilder()
                .add(new MockI2CProviderImpl())
                .build();

        try (var registry = new Pi4jDeviceFactory(context).asDeviceRegistry();
             var device = registry.create(I2CConfig.builder().bus(1).build())) {
            device.transfer(new I2C.Message(0x10, new byte[]{(byte) 0xFF}, 0, 1, I2C.Message.Type.WRITE));

            assertThat(registry.contains(device.getConfig().getId())).isTrue();
            assertThat(context.registry().all().size()).isEqualTo(1);
        }

        assertThat(context.registry().all().size()).isEqualTo(0);
    }
}