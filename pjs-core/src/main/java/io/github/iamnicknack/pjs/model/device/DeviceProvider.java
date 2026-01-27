package io.github.iamnicknack.pjs.model.device;

/**
 * An object which can create a {@link Device} of type T, from a {@link DeviceConfig} of type V.
 * @param <T> the type of device created by this provider.
 * @param <V> the type of configuration used to create the device.
 */
public interface DeviceProvider<T extends Device<T>, V extends DeviceConfig<T>> extends AutoCloseable {

    /**
     * Create a device of type T, from a configuration of type V.
     * @param config the configuration to create the device from.
     * @return the created device.
     */
    T create(V config);

}
