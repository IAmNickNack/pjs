package io.github.iamnicknack.pjs.ffm.device.context;

import java.lang.foreign.MemorySegment;
import java.util.function.BiFunction;

public class AbstractFileOperations implements FileOperations {

    @Override
    public int open(String pathname, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileDescriptor openFd(String pathname, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int close(int fd) {
        // do nothing
        return 0;
    }

    @Override
    public int read(int fd, byte[] buffer, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T read(int fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(int fd, byte[] buffer, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int access(String pathname, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String pathname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int isValid(int fd) {
        return 1;
    }

    @Override
    public FileDescriptor createFileDescriptor(int fd) {
        return new FileDescriptor(this, fd);
    }
}
