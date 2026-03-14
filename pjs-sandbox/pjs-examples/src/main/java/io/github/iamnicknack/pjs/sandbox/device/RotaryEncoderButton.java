package io.github.iamnicknack.pjs.sandbox.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.model.ReadOperation;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;

/**
 * A button which can be attached to a rotary encoder. The button is configured to capture the current value of the
 * encoder and fire an event when pressed.
 * @see RotaryEncoder
 */
public class RotaryEncoderButton extends GpioEventEmitterDelegate<RotaryEncoderButton> implements ReadOperation<Integer> {
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
    private void onButtonEvent(GpioChangeEvent<GpioPort> event) {
        this.value = this.encoder.read();
        this.onEvent(new GpioChangeEvent<>(this, event.eventType()));
    }
}
