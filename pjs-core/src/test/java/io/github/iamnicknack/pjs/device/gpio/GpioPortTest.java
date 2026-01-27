package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.mock.MockGpioPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class GpioPortTest {

    @Test
    void canCreateMaskedPin() {
        var port = new MockGpioPort(GpioPortConfig.builder().pin(0, 2).build());
        var pin = port.pin();

        pin.high();
        Assertions.assertThat(port.read()).isEqualTo(0b11);
    }

}