package io.github.iamnicknack.pjs.model.device;

/**
 * Configuration object for a device of type T.
 * <p>
 * Configurations are used by a {@link DeviceFactory} to create a device of type T.
 * </p>
 * @param <T> the device type
 */
public interface DeviceConfig<T> {

    /**
     * An arbitrary unique identifier for the device created using this configuration
     */
    String getId();
}
