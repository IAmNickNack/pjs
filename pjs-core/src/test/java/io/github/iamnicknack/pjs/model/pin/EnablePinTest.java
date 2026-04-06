package io.github.iamnicknack.pjs.model.pin;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry;
import io.github.iamnicknack.pjs.mock.MockDeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class EnablePinTest {

    private final DeviceRegistry registry = new LoggingDeviceRegistry(new MockDeviceRegistry());
    private final Pin pin = registry.create(
                    GpioPortConfig.builder()
                            .id("pin")
                            .pin(0)
                            .portMode(GpioPortMode.OUTPUT)
                            .build()
            )
            .pin();

    @TestFactory
    Stream<DynamicTest> setsActiveState() {
        return Stream.of(
                new Expectation(EnablePin.activeHigh(pin), true),
                new Expectation(EnablePin.activeLow(pin), false)
        ).map(expectation -> dynamicTest(expectation.activeState ? "active high" : "active low", () -> {
            pin.write(!expectation.activeState);
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
            expectation.enablePin.enable();
            assertThat(expectation.enablePin.read()).isEqualTo(expectation.activeState);;
            expectation.enablePin.disable();
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
        }));
    }

    @TestFactory
    Stream<DynamicTest> enablesAroundOperation() {
        return Stream.of(
                new Expectation(EnablePin.activeHigh(pin), true),
                new Expectation(EnablePin.activeLow(pin), false)
        ).map(expectation -> dynamicTest(expectation.activeState ? "active high" : "active low", () -> {
            pin.write(!expectation.activeState);
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
            expectation.enablePin.enableAround(() -> {
                assertThat(expectation.enablePin.read()).isEqualTo(expectation.activeState);
            });
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
        }));
    }

    @TestFactory
    Stream<DynamicTest> enablesAroundOperationAndReturnsValue() {
        return Stream.of(
                new Expectation(EnablePin.activeHigh(pin), true),
                new Expectation(EnablePin.activeLow(pin), false)
        ).map(expectation -> dynamicTest(expectation.activeState ? "active high" : "active low", () -> {
            pin.write(!expectation.activeState);
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
            var result = expectation.enablePin.enableAround(() -> {
                assertThat(expectation.enablePin.read()).isEqualTo(expectation.activeState);
                return 1;
            });
            assertThat(expectation.enablePin.read()).isEqualTo(!expectation.activeState);
            assertThat(result).isEqualTo(1);
        }));
    }


    record Expectation(
            EnablePin enablePin,
            boolean activeState
    ) {}
}