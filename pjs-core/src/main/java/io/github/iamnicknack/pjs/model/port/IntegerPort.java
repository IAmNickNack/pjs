package io.github.iamnicknack.pjs.model.port;

import java.util.function.Function;

/**
 * A port implementation which most closely matches a hardware implementation of a port.
 * <p>
 * Unlike more generic ports, this implementation can be masked to a specific range of pins.
 * </p>
 */
public class IntegerPort implements Port<Integer> {

    private final Port<Integer> delegate;

    public IntegerPort(Port<Integer> delegate) {
        this.delegate = delegate;
    }

    public <T> IntegerPort(
            Port<T> delegate,
            Function<T, Integer> readMapper,
            Function<Integer, T> writeMapper
    ) {
        this.delegate = delegate.mapped(readMapper, writeMapper);
    }

    @Override
    public Integer read() {
        return delegate.read();
    }

    @Override
    public void write(Integer value) {
        delegate.write(value);
    }

    public Port<Integer> masked(int mask) {
        return new Port<>() {
            @Override
            public Integer read() {
                return IntegerPort.this.read() & mask;
            }

            @Override
            public void write(Integer value) {
                int current = IntegerPort.this.read();
                current = current & ~mask;
                current = current | (value & mask);
                IntegerPort.this.write(current);
            }
        };
    }
}
