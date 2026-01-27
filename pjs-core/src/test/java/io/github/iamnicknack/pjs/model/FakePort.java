package io.github.iamnicknack.pjs.model;

import io.github.iamnicknack.pjs.model.port.Port;

public class FakePort implements Port<Integer> {
    public int value;

    public FakePort(int value) {
        this.value = value;
    }

    @Override
    public Integer read() {
        return this.value;
    }

    @Override
    public void write(Integer value) {
        this.value = value;
    }
}
