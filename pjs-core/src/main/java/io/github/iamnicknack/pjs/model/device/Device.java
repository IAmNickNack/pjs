package io.github.iamnicknack.pjs.model.device;

/**
 * Common functionality required by all devices which implement driver-like behaviours.
 * @param <T> The specialisation extending this interface.
 *           This is used to ensure that only compatible device-configuration pairs are used in code
 */
public interface Device<T> extends AutoCloseable {
    DeviceConfig<T> getConfig();

//    @SuppressWarnings("unchecked")
//    default T getDelegate() {
//        return (T)this;
//    }
//
    @Override
    default void close() throws Exception {}
}
