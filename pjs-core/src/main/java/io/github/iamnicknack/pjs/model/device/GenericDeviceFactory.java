package io.github.iamnicknack.pjs.model.device;

import io.github.iamnicknack.pjs.impl.GenericDeviceFactoryBuilder;
import io.github.iamnicknack.pjs.impl.TrackingDeviceFactory;

/**
 * A generic function which can create a device of type T, from a configuration of type V.
 */
@FunctionalInterface
public interface GenericDeviceFactory {
    /**
     * Create a device of type T, from a configuration of type V.
     * @param config the configuration to create the device from.
     * @return the created device.
     * @param <T> the type of device created by this factory.
     * @param <V> the type of configuration used to create the device.
     */
    <T extends Device<T>, V extends DeviceConfig<T>> T create(V config);

    /**
     * Create a device registry which uses this factory.
     * @return the created device registry.
     */
    default DeviceRegistry asDeviceRegistry() {
        if (this instanceof DeviceRegistry) {
            return (DeviceRegistry) this;
        }
        return new TrackingDeviceFactory(this);
    }

    /**
     * Create a builder for a generic device factory.
     * @return the created builder.
     */
    static GenericDeviceFactoryBuilder builder() {
        return new GenericDeviceFactoryBuilder();
    }
}
