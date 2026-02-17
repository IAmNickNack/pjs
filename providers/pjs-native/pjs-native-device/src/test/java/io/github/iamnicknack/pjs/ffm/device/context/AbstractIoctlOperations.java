package io.github.iamnicknack.pjs.ffm.device.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of {@link IoctlOperations} for testing purposes.
 * This allows individual methods to be overridden as required by when components have a
 * dependency on a subset of functions provided by {@link IoctlOperations}.
 */
public abstract class AbstractIoctlOperations implements IoctlOperations {

    private final Map<Long, Handler> handlers;

    public AbstractIoctlOperations() {
        this.handlers = new HashMap<>();
    }

    AbstractIoctlOperations(Map<Long, Handler> handlers) {
        this.handlers = handlers;
    }

    private Object handle(int fd, long command, Object data) {
        if (handlers.containsKey(command)) {
            return handlers.get(command).ioctl(fd, command, data);
        }
        throw new UnsupportedOperationException(Long.toString(command));
    }

    @Override
    public int ioctl(int fd, long command, int data) {
        return (int)handle(fd, command, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T ioctl(int fd, long command, T data, Class<T> type) {
        return (T)handle(fd, command, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T ioctl(int fd, long command, T data) {
        return (T)handle(fd, command, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T ioctl(int fd, long command, Class<T> type) {
        return (T)handle(fd, command, null);
    }

    @FunctionalInterface
    public interface Handler {
        Object ioctl(int fd, long command, Object data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Long, Handler> handlers = new HashMap<>();

        public Builder addHandler(Long command, Handler handler) {
            handlers.put(command, handler);
            return this;
        }

        public Builder addHandler(Long command) {
            return addHandler(command, (_, _, data) -> data);
        }

        public AbstractIoctlOperations build() {
            return new AbstractIoctlOperations(handlers) {};
        }
    }
}
