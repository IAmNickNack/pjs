package io.github.iamnicknack.pjs.model.device;

/**
 * A generic function which can create a device of type T, from a configuration of type V.
 */
@FunctionalInterface
public interface GenericDeviceProvider {
    /**
     * Create a device of type T, from a configuration of type V.
     * @param config the configuration to create the device from.
     * @return the created device.
     * @param <T> the type of device created by this provider.
     * @param <V> the type of configuration used to create the device.
     */
    <T extends Device<T>, V extends DeviceConfig<T>> T create(V config);
}
