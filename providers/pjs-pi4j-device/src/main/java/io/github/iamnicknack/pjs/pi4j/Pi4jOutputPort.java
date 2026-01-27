package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.io.gpio.digital.DigitalOutput;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;

import java.util.List;

public class Pi4jOutputPort implements GpioPort {

    private final GpioPortConfig config;
    private final List<DigitalOutput> digitalOutputs;
    private final Runnable onClose;

    public Pi4jOutputPort(
            GpioPortConfig config,
            List<DigitalOutput> digitalOutputs,
            Runnable onClose
    ) {
        this.config = config;
        this.digitalOutputs = digitalOutputs;
        this.onClose = onClose;
    }

    @Override
    public DeviceConfig<GpioPort> getConfig() {
        return config;
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        // do nothing or throw exception?
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        // do nothing or throw exception?
    }

    @Override
    public Integer read() {
        int result = 0;

        for (int i = 0; i < digitalOutputs.size(); i++) {
            DigitalOutput digitalOutput = digitalOutputs.get(i);
            if (digitalOutput.isHigh()) {
                result |= 1 << i;
            }
        }

        return result;
    }

    @Override
    public void write(Integer value) {
        for (int i = 0; i < digitalOutputs.size(); i++) {
            DigitalOutput digitalOutput = digitalOutputs.get(i);
            digitalOutput.setState((value & (1 << i)) != 0);
        }
    }

    @Override
    public void close() {
        this.onClose.run();
    }
}
