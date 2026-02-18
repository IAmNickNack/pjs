package io.github.iamnicknack.pjs.ffm.device.context;

import java.nio.file.Path;

public abstract class AbstractSysfsOperations implements SysfsOperations {

    public final String devicePath;

    public AbstractSysfsOperations(String devicePath) {
        this.devicePath = devicePath;
    }

    @Override
    public Path path() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(String path, byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] read(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeString(String path, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readString(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLong(String path, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readLong(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInt(String path, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readInt(String path) {
        throw new UnsupportedOperationException();
    }
}
