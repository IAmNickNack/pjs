package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.pin.PinOperations;

/**
 * Simple implementation of {@link PinOperations}
 */
public class DefaultPinOperations implements PinOperations {
    private final Pin pin;
    private volatile boolean state;

    /**
     * Create a new operations instance
     * @param pin the pin to delegate to
     */
    public DefaultPinOperations(Pin pin) {
        this.pin = pin;
        this.state = pin.read();
    }

    @Override
    public Boolean read() {
        return pin.read();
    }

    @Override
    public void write(Boolean value) {
        pin.write(value);
        this.state = value;
    }

    @Override
    public void pulse() {
        write(!state);
        write(!state);
    }

    @Override
    public void toggle() {
        write(!state);
    }
}
