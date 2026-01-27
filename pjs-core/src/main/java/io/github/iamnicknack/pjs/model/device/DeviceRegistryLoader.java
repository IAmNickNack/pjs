package io.github.iamnicknack.pjs.model.device;


import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Configure a device registry from a map of properties.
 * Delegating construction of a registry to the loader allows the loading
 */
public interface DeviceRegistryLoader {

    /**
     * Assert that the registry can be loaded from the given properties.
     * @param properties the properties to initialise with.
     * @return true if the registry can be loaded, false otherwise.
     */
    boolean isLoadable(Map<String, Object> properties);

    /**
     * Assert that the registry can be loaded from the given properties.
     * @param properties the properties to initialise with.
     * @return true if the registry can be loaded, false otherwise.
     */
    default boolean isLoadable(Properties properties) {
        return isLoadable(toMap(properties));
    }

    /**
     * Assert that the registry can be loaded from the system properties.
     * @return true if the registry can be loaded, false otherwise.
     */
    default boolean isLoadable() {
        return isLoadable(System.getProperties());
    }

    /**
     * Load a device registry from the given properties.
     * @param properties the properties to load from.
     * @return the loaded device registry, or null if no device registry could be configured.
     */
    @Nullable
    DeviceRegistry load(Map<String, Object> properties);

    /**
     * Load a device registry from the provided java properties
     * @param properties the properties to load from.
     * @return the loaded device registry, or null if no device registry could be configured.
     */
    @Nullable
    default DeviceRegistry load(Properties properties) {
        return load(toMap(properties));
    }

    /**
     * Load a device registry from the system properties.
     * @return the loaded device registry, or null if no device registry could be configured.
     */
    @Nullable
    default DeviceRegistry load() {
        return load(System.getProperties());
    }

    private static Map<String, Object> toMap(Properties properties) {
        return properties.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    /**
     * Load the default device registry, providing any configuration only from system properties.
     * @return the default device registry.
     */
    static DeviceRegistry defaultRegistry() {
        return ServiceLoader.load(DeviceRegistryLoader.class, DeviceRegistryLoader.class.getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .filter(DeviceRegistryLoader::isLoadable)
                .findFirst()
                .map(DeviceRegistryLoader::load)
                .orElseThrow(() -> new IllegalStateException("No device registry loader found."));

    }
}
