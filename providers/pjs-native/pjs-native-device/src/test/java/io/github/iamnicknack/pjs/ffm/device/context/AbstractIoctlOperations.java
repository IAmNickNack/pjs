package io.github.iamnicknack.pjs.ffm.device.context;

/**
 * Abstract implementation of {@link IoctlOperations} for testing purposes.
 * This allows individual methods to be overridden as required by when components have a
 * dependency on a subset of functions provided by {@link IoctlOperations}.
 */
public abstract class AbstractIoctlOperations implements IoctlOperations {

    @Override
    public int ioctl(int fd, long command, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T ioctl(int fd, long command, T data, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T ioctl(int fd, long command, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T ioctl(int fd, long command, Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
