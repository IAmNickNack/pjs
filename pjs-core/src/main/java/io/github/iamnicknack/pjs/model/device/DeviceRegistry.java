package io.github.iamnicknack.pjs.model.device;

import org.jspecify.annotations.Nullable;

/**
 * Intended to be used as a factory for {@link Device} instances.
 * <p>
 * A registry can be used to assist in the lifecycle management of {@link Device} instances. Potentially
 * ensuring that resources are properly disposed of when no longer needed.
 * </p>
 */
public interface DeviceRegistry extends GenericDeviceFactory, Iterable<Device<?>>, AutoCloseable {

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
     * TODO: Maybe remove this
     */
    @Override
    default void close() {}

    /**
     * Wrapper exception for registry errors.
     */
    class RegistryException extends RuntimeException {
        public RegistryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
