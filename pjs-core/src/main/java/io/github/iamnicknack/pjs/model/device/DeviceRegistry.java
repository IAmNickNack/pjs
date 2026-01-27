package io.github.iamnicknack.pjs.model.device;

import org.jspecify.annotations.Nullable;

/**
 * Intended to be used as a factory for {@link Device} instances.
 * <p>
 * A registry can be used to assist in the lifecycle management of {@link Device} instances. Potentially
 * ensuring that resources are properly disposed of when no longer needed.
 * </p>
 */
public interface DeviceRegistry extends GenericDeviceProvider, Iterable<Device<?>>, AutoCloseable {

    /**
     * Register a device provider.
     * @param provider the provider to register.
     * @param configType the configuration type used by the provider.
     * @return this instance for chaining.
     * @param <T> the type of device created by the provider.
     * @param <V> the type of configuration used by the provider.
     */
    <T extends Device<T>, V extends DeviceConfig<T>> DeviceRegistry registerProvider(
            DeviceProvider<T, V> provider,
            Class<V> configType
    );

    /**
     * Get the provider associated with a given configuration type.
     * @param configType the configuration type.
     * @return the provider, or null if not found.
     * @param <T> the type of device created by the provider.
     */
    <T extends Device<T>, V extends DeviceConfig<T>> DeviceProvider<T, V> getProvider(Class<V> configType);

    /**
     * Create a device of type T, from a configuration of type V.
     * @param config the configuration to create the device from.
     * @return the created device.
     * @param <T> the type of device created by this provider.
     * @param <V> the type of configuration used to create the device.
     */
    @Override
    <T extends Device<T>, V extends DeviceConfig<T>> T create(V config);

    /**
     * Remove a device instance from the registry.
     * @param device the device to remove.
     */
    void remove(Device<?> device);

    /**
     * Remove a device instance from the registry.
     * @param id the id of the device to remove.
     */
    void remove(String id);

    /**
     * Get a device instance by id.
     * @param id the id of the device to retrieve.
     * @param deviceType the type of device to retrieve.
     * @return the device instance, or null if not found.
     * @param <T> the type of device to retrieve.
     */
    @Nullable
    <T extends Device<T>> T device(String id, Class<T> deviceType);

    /**
     * Check if a device with the given id is registered.
     * @param id the id to check.
     * @return true if the device is registered, false otherwise.
     */
    boolean contains(String id);

    /**
     * Close the registry and release any resources.
     */
    @Override
    void close();

    /**
     * Wrapper exception for registry errors.
     */
    class RegistryException extends RuntimeException {
        public RegistryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
