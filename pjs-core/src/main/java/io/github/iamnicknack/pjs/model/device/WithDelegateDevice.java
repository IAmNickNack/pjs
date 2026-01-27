package io.github.iamnicknack.pjs.model.device;

/**
 * Indication that a device delegates to another device.
 * @param <T> the delegate device type
 */
public interface WithDelegateDevice<T extends Device<T>> {
    T getDelegate();
}
