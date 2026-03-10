package io.github.iamnicknack.pjs.sandbox.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.model.ReadOperation;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitter;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;

public class RotaryEncoder implements GpioEventEmitter<RotaryEncoder>, ReadOperation<Integer> {
    private final GpioEventEmitterDelegate<RotaryEncoder> delegate = new GpioEventEmitterDelegate<>();
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

        var delta = (diff < 30) ? 3 : 1;

        var portValue = event.port().read();
        if (portValue == 1) {
            // clockwise
            var v = value;
            value = (Math.floorDiv(v, delta) * delta) + delta;
            delegate.onEvent(new GpioChangeEvent<>(this, GpioChangeEventType.RISING));
        } else if (portValue == 2) {
            // anti-clockwise
            var v = value;
            value = (Math.floorDiv(v, delta) * delta) - delta;
            delegate.onEvent(new GpioChangeEvent<>(this, GpioChangeEventType.FALLING));
        }
    }

    @Override
    public void addListener(GpioEventListener<RotaryEncoder> listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(GpioEventListener<RotaryEncoder> listener) {
        delegate.removeListener(listener);
    }
}
