package io.github.iamnicknack.pjs.model.pin;

import io.github.iamnicknack.pjs.model.port.Port;

import java.util.function.Function;

/**
 * A pin implementation which most closely matches the behaviour expected of a digital pin.
 */
public class BooleanPin implements Pin {

    private final Port<Boolean> delegate;

    public BooleanPin(Port<Boolean> delegate) {
        this.delegate = delegate;
    }

    public <T> BooleanPin(
            Port<T> delegate,
            Function<T, Boolean> readMapper,
            Function<Boolean, T> writeMapper
    ) {
        this.delegate = delegate.mapped(readMapper, writeMapper);
    }

    @Override
    public void high() {
        delegate.write(true);
    }

    @Override
    public void low() {
        delegate.write(false);
    }

    @Override
    public Boolean read() {
        return delegate.read();
    }

    @Override
    public void write(Boolean value) {
        delegate.write(value);
    }
}
