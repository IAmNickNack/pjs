package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceProvider;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Registry to help manage the lifecycle of providers and devices
 */
public class DefaultDeviceRegistry implements DeviceRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<Class<?>, DeviceProvider<?, ?>> providers = new HashMap<>();
    private final Map<String, Device<?>> devices = new HashMap<>();

    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> DeviceRegistry registerProvider(
            DeviceProvider<T, V> provider,
            Class<V> configType
    ) {
        providers.put(configType, provider);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> DeviceProvider<T, V> getProvider(Class<V> configType) {
        if (providers.containsKey(configType)) {
            return (DeviceProvider<T, V>) providers.get(configType);
        }
        throw new IllegalArgumentException("No provider registered for config type: " + configType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        try {
            var provider = (DeviceProvider<T, DeviceConfig<T>>) providers.get(config.getClass());
            if (provider == null) {
                throw new RegistryException(
                        "No provider registered for config type: " + config.getClass(),
                        new IllegalArgumentException()
                );
            }
            var device = provider.create(config);
            devices.put(config.getId(), device);
            logger.info("Created {} device with id: {}", device.getClass().getSimpleName(), config.getId());
            return device;
        } catch (Exception e) {
            logger.error("Failed to create device with id: {}", config.getId(), e);
            throw e;
        }
    }

    @Override
    public void remove(Device<?> device) {
        remove(device.getConfig().getId());
    }

    @Override
    public void remove(String id) {
        var device = devices.remove(id);
        if (device != null) {
            logger.info("Removing device: {}, {}", id, device.getClass().getName());
            try {
                device.close();
            } catch (Exception e) {
                throw new RegistryException("Failed to remove device " + id, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>> T device(String id, Class<T> deviceType) {
        var device = devices.get(id);
        if (device != null && deviceType.isAssignableFrom(device.getClass())) {
            return (T)device;
        }
        return null;
    }

    @Override
    public boolean contains(String id) {
        return devices.containsKey(id);
    }

    @Override
    public Iterator<Device<?>> iterator() {
        return devices.values().iterator();
    }

    @Override
    public void close() {
        providers.values().forEach(deviceProvider -> {
            try {
                logger.info("Closing provider: {}", deviceProvider.getClass().getName());
                deviceProvider.close();
            } catch (Exception e) {
                // don't rethrow, just log.
                // we want to attempt to close all providers.
                logger.error("Failed to close device provider: {}", deviceProvider, e);
            }
        });

        devices.values().forEach(device -> {
            try {
                logger.info("Closing device: {}, {}", device.getConfig().getId(), device.getClass().getName());
                device.close();
            } catch (Exception e) {
                // don't rethrow, just log.
                // we want to attempt to close all devices.
                logger.error("Failed to close device: {}", device, e);
            }
        });
    }

}
