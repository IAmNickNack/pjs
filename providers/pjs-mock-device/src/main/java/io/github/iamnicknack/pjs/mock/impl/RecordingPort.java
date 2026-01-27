package io.github.iamnicknack.pjs.mock.impl;

import io.github.iamnicknack.pjs.model.port.Port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Port implementation which records all values written to it.
 * @param <T> the type of values written to the port
 */
public class RecordingPort<T> implements Port<T> {

    private final List<T> values;

    private final Port<T> delegate;

    public RecordingPort(Port<T> delegate) {
        this.delegate = delegate;
        this.values = new ArrayList<>();
    }

    public void clear() {
        values.clear();
    }

    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public T read() {
        return delegate.read();
    }

    @Override
    public void write(T value) {
        delegate.write(value);
        values.add(value);
    }
}

