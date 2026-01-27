package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.mock.MockGpioPort;
import org.junit.jupiter.api.Test;

class LoggingGpioPortTest {

    @Test
    void writesToDelegatePort() {
        var gpioPort = new MockGpioPort(GpioPortConfig.builder().pin(1).build());
        var loggingPort = new LoggingGpioPort(gpioPort);

        loggingPort.write(1);
        var v = loggingPort.read();
        assert v == 1;

        v = gpioPort.read();
        assert v == 1;
    }
}