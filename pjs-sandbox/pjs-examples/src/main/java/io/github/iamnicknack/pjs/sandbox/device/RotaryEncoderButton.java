package io.github.iamnicknack.pjs.sandbox.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.model.ReadOperation;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitter;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;

public class RotaryEncoderButton implements GpioEventEmitter<RotaryEncoderButton>, ReadOperation<Integer> {
    private final GpioEventEmitterDelegate<RotaryEncoderButton> delegate = new GpioEventEmitterDelegate<>();
    private volatile int value = 0;
    private final RotaryEncoder encoder;

    public RotaryEncoderButton(RotaryEncoder encoder, GpioPort buttonPort) {
        this.encoder = encoder;
        buttonPort.addListener(this::onButtonEvent);
    }

    @Override
    public Integer read() {
        return value;
    }

    @Override
    public void addListener(GpioEventListener<RotaryEncoderButton> listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(GpioEventListener<RotaryEncoderButton> listener) {
        delegate.removeListener(listener);
    }

    private void onButtonEvent(GpioChangeEvent<GpioPort> event) {
        this.value = this.encoder.read();
        this.delegate.onEvent(new GpioChangeEvent<>(this, event.eventType()));
    }
}
