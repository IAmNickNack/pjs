package io.github.iamnicknack.pjs.model;


/**
 * An operation to read a value from a device.
 * @param <T> the type of value read from the device
 */
@FunctionalInterface
public interface ReadOperation<T> {
    /**
     * Read a value from the device.
     * @return the value read.
     */
    T read();
}
