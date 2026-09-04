package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.logging.LoggingDeviceFactory;
import io.github.iamnicknack.pjs.mock.MockDeviceFactory;
import io.github.iamnicknack.pjs.mock.MockI2CFactory;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class GenericDeviceFactoryBuilderTest {

    @Test
    void canBuildFactories() throws Exception {
        var decoratorCount = new AtomicInteger(0);

        var factory = GenericDeviceFactory.builder()
                .factory(MockDeviceFactory::new)
                .decorator(root -> {
                    assertThat(decoratorCount.getAndIncrement()).isEqualTo(0);
                    return new LoggingDeviceFactory(root);
                })
                .decorator(root -> {
                    assertThat(decoratorCount.getAndIncrement()).isEqualTo(1);
                    return root;
                })
                .build();

        assertThat(decoratorCount.get()).isEqualTo(2);

        try (var gpio = factory.create(GpioPortConfig.builder().id("test").pin(1).build())) {
            gpio.write(0xFF);
            assertThat(gpio.read()).isEqualTo(0xFF);
        }
    }

    @Test
    void cannotBuildEmptyFactory() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> GenericDeviceFactory.builder().build());
    }

    @Test
    void cannotAddConflictingFactoryTypes() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> GenericDeviceFactory.builder()
                        .factory(MockDeviceFactory::new)
                        .factory(new MockI2CFactory(), I2CConfig.class)
                        .build()
                );

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> GenericDeviceFactory.builder()
                        .factory(new MockI2CFactory(), I2CConfig.class)
                        .factory(MockDeviceFactory::new)
                        .build()
                );
    }

    @Test
    void cannotAddDuplicateFactoryTypes() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> GenericDeviceFactory.builder()
                        .factory(new MockI2CFactory(), I2CConfig.class)
                        .factory(new MockI2CFactory(), I2CConfig.class)
                        .build()
                );

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> GenericDeviceFactory.builder()
                        .factory(MockDeviceFactory::new)
                        .factory(MockDeviceFactory::new)
                        .build()
                );
    }
}