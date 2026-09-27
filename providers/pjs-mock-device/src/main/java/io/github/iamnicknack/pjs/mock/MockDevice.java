package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.model.device.Device;

public interface MockDevice<T extends Device<T>> {

    boolean isClosed();

    class Default<T extends Device<T>> implements MockDevice<T>, AutoCloseable {

        private boolean isClosed = false;

        public boolean isClosed() {
            return this.isClosed;
        }

        @Override
        public void close() throws Exception {
            this.isClosed = true;
        }
    }
}
