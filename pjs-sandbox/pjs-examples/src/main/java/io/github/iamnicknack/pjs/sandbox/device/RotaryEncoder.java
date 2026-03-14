package io.github.iamnicknack.pjs.sandbox.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.model.ReadOperation;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;

/**
 * Basic implementation of a rotary encoder.
 * <p>
 * Utilises a {@link io.github.iamnicknack.pjs.model.port.Port} of two pins representing channels A on pin 0 and B
 * on pin 1.
 * A single interrupt is configured to fire each time this port value changes. There are no separate interrupts
 * for A and B.
 * <p>
 * If A falls before B, the input is binary 2 (0b10). If B falls before A, the input is binary 1 (0b01).
 * A value of 1 indicates a clockwise rotation, while a value of 2 indicates an anti-clockwise rotation.
 * <p>
 * A clockwise change event increments the current value and is indicated with an event type of
 * {@link GpioChangeEventType#RISING}. An anti-clockwise change event decrements the current value and is
 * indicated with an event type of {@link GpioChangeEventType#FALLING}.
 */
public class RotaryEncoder extends GpioEventEmitterDelegate<RotaryEncoder> implements ReadOperation<Integer> {
    private volatile int value = 0;   // written on callback thread, read elsewhere
    private long lastEventTime = 0L;  // callback-thread only

    public RotaryEncoder(GpioPort rotaryPort) {
        rotaryPort.addListener(this::onEncoderEvent);
    }

    @Override
    public Integer read() {
        return value;
    }

    private void onEncoderEvent(GpioChangeEvent<GpioPort> event) {
        var now = System.currentTimeMillis();
        var diff = now - lastEventTime;
        lastEventTime = now;

        // larger delta value depending on how fast the encoder is spinning
        var delta = (diff < 30) ? 3 : 1;

        var portValue = event.port().read();
        if (portValue == 1) {
            // clockwise
            var v = value;
            value = (Math.floorDiv(v, delta) * delta) + delta;
            onEvent(new GpioChangeEvent<>(this, GpioChangeEventType.RISING));
        } else if (portValue == 2) {
            // anti-clockwise
            var v = value;
            value = (Math.floorDiv(v, delta) * delta) - delta;
            onEvent(new GpioChangeEvent<>(this, GpioChangeEventType.FALLING));
        }
    }
}
