package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Registry to help manage the lifecycle of factories and devices
 */
public class DefaultDeviceRegistry implements DeviceRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<Class<?>, DeviceFactory<?, ?>> factories = new HashMap<>();
    private final Map<String, Device<?>> devices = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> DeviceRegistry registerFactory(
            DeviceFactory<T, V> factory,
            Class<V> configType
    ) {
        factories.put(configType, factory);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> DeviceFactory<T, V> getFactory(Class<V> configType) {
        if (factories.containsKey(configType)) {
            return (DeviceFactory<T, V>) factories.get(configType);
        }
        throw new IllegalArgumentException("No factory registered for config type: " + configType);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        try {
            var factory = (DeviceFactory<T, DeviceConfig<T>>) factories.get(config.getClass());
            if (factory == null) {
                throw new RegistryException(
                        "No factory registered for config type: " + config.getClass(),
                        new IllegalArgumentException()
                );
            }
            var device = factory.create(config);
            devices.put(config.getId(), device);
            logger.info("Created {} device with id: {}", device.getClass().getSimpleName(), config.getId());
            return device;
        } catch (Exception e) {
            logger.error("Failed to create device with id: {}", config.getId(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Device<?> device) {
        remove(device.getConfig().getId());
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Device<T>> T device(String id, Class<T> deviceType) {
        var device = devices.get(id);
        if (device != null && deviceType.isAssignableFrom(device.getClass())) {
            return (T)device;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String id) {
        return devices.containsKey(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Device<?>> iterator() {
        return devices.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        factories.values().forEach(deviceFactory -> {
            try {
                logger.info("Closing factory: {}", deviceFactory.getClass().getName());
                deviceFactory.close();
            } catch (Exception e) {
                // don't rethrow, just log.
                // we want to attempt to close all factories.
                logger.error("Failed to close device factory: {}", deviceFactory, e);
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

    /**
     * Append a device to the registry. Used by descendent classes to append devices which could be lazily created.
     * <p>
     * E.g. A remote device factory could eagerly create a device which gets added to the registry as normal, but
     * not created via the client-side registry.
     * <p>
     * The intention here is to allow the client-side registry to know about devices which are managed by remote factories.
     * @param device the device to append
     */
    protected void appendDevice(Device<?> device) {
        if (!devices.containsKey(device.getConfig().getId())) {
            devices.put(device.getConfig().getId(), device);
        }
    }
}
