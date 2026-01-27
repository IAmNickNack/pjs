package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.mock.MockGpioPort;
import io.github.iamnicknack.pjs.mock.impl.RecordingPort;
import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.pin.PinOperations;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultPinOperationsTest {

    @Test
    void canReadValuesFromPort() {
        MockGpioPort port = new MockGpioPort(GpioPortConfig.builder().pin(1).build());
        RecordingPort<Integer> recordingPort = new RecordingPort<>(port);
        Pin pin = recordingPort.pin(1, 0);

        pin.write(true);
        Assertions.assertThat(recordingPort.getValues()).containsExactly(1);
    }

    @Test
    void canPulsePin() {
        MockGpioPort port = new MockGpioPort(GpioPortConfig.builder().pin(1).build());
        RecordingPort<Integer> recordingPort = new RecordingPort<>(port);
        Pin pin = recordingPort.pin(1, 0);
        PinOperations operations = new DefaultPinOperations(pin);

        operations.pulse();
        Assertions.assertThat(recordingPort.getValues()).containsExactly(1, 0);

        operations.high();
        recordingPort.clear();
        operations.pulse();
        Assertions.assertThat(recordingPort.getValues()).containsExactly(0, 1);
    }
}