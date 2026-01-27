package io.github.iamnicknack.pjs.model;

import io.github.iamnicknack.pjs.model.port.SerialPort;

import java.nio.ByteBuffer;

public class FakeSerialPort implements SerialPort {

    private final ByteBuffer buffer;

    public FakeSerialPort() {
        this.buffer = ByteBuffer.allocate(1024);
    }

    public FakeSerialPort(byte[] initialBuffer) {
        this.buffer = ByteBuffer.wrap(initialBuffer);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        this.buffer.get(buffer, offset, length);
        return length;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        this.buffer.put(buffer, offset, length);
    }
}
