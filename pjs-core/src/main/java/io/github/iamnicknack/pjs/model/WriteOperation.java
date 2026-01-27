package io.github.iamnicknack.pjs.model;

/**
 * An operation to write a value to a device.
 * @param <T>
 */
@FunctionalInterface
public interface WriteOperation<T> {
    /**
     * Write the given value to the device.
     * @param value the value to write.
     */
    void write(T value);
}
