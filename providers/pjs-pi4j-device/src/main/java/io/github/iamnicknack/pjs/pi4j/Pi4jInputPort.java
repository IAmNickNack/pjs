package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;

import java.util.List;

public class Pi4jInputPort implements GpioPort {

    private final GpioPortConfig config;
    private final List<DigitalInput> digitalInputs;
    private final Runnable onClose;

    public Pi4jInputPort(
            GpioPortConfig config,
            List<DigitalInput> digitalInputs,
            Runnable onClose
    ) {
        this.config = config;
        this.digitalInputs = digitalInputs;
        this.onClose = onClose;
    }

    @Override
    public DeviceConfig<GpioPort> getConfig() {
        return this.config;
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        digitalInputs.forEach(input -> input.addListener(mapListener(listener)));
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        digitalInputs.forEach(input -> input.removeListener(mapListener(listener)));
    }

    @Override
    public Integer read() {
        int result = 0;

        for (int i = 0; i < digitalInputs.size(); i++) {
            DigitalInput digitalInput = digitalInputs.get(i);
            if (digitalInput.isHigh()) {
                result |= 1 << i;
            }
        }

        return result;
    }

    @Override
    public void write(Integer value) {
        // do nothing
    }

    @Override
    public void close() {
        this.onClose.run();
    }

    private DigitalStateChangeListener mapListener(GpioEventListener<GpioPort> listener) {
        return event -> {
            var type = (event.state().isHigh()) ? GpioChangeEventType.RISING : GpioChangeEventType.FALLING;
            listener.onEvent(new GpioChangeEvent<>(Pi4jInputPort.this, type));
        };
    }
}
