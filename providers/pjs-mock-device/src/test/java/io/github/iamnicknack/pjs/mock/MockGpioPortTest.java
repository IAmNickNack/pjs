package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class MockGpioPortTest {

    private final GpioPortConfig outputConfig = GpioPortConfig.builder()
            .id("mock-output")
            .pin(1)
            .portMode(GpioPortMode.OUTPUT)
            .build();

    private final GpioPortConfig inputConfig = GpioPortConfig.builder()
            .id("mock-input")
            .pin(1)
            .portMode(GpioPortMode.INPUT)
            .build();

    @Test
    void canWritePort() {
        MockGpioPort port = new MockGpioPort(outputConfig);
        port.write(1);
        assertThat(port.read()).isEqualTo(1);
    }

    @Test
    void canReadPort() {
        var mock = new MockGpioPort(inputConfig);
        mock.write(1);
        assertThat(mock.read()).isEqualTo(1);
    }

    @Test
    void portRaisesInputEvents() throws InterruptedException {
        var mock = new MockGpioPort(inputConfig);
        var invoked = new AtomicBoolean(false);
        var latch = new CountDownLatch(1);
        mock.addListener(_ -> {
            invoked.set(true);
            latch.countDown();
        });
        mock.mockValue(42);
        latch.await(1, TimeUnit.SECONDS);
        assertThat(invoked.get()).isTrue();
        assertThat(mock.read()).isEqualTo(42);
    }

}