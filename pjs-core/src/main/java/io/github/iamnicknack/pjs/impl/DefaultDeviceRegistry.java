package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Registry to help manage the lifecycle of factories and devices
 */
@Deprecated
public class DefaultDeviceRegistry implements DeviceRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Device<?>> devices = new HashMap<>();
    private final Map<Class<?>, DeviceFactory<?, ?>> factories = new HashMap<>();

    /**
     * Register a device factory.
     * @param factory the factory to register.
     * @param configType the configuration type used by the factory.
     * @return this instance for chaining.
     * @param <T> the type of device created by the factory.
     * @param <V> the type of configuration used by the factory.
     */
    public final <T extends Device<T>, V extends DeviceConfig<T>> DeviceRegistry registerFactory(
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
            logger.atInfo().log("Created {} device with id: {}", device.getClass().getSimpleName(), config.getId());

            // provide the user with a proxy that hooks into the close method to keep the registry up to date
            return interceptClose(device);
        } catch (Exception e) {
            logger.atError().log("Failed to create device with id: {}", config.getId(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Device<?> device) {
        remove(device.getConfig().getId());
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String id) {
        var device = devices.remove(id);
        if (device != null) {
            logger.atInfo().log("Removing device: {}, {}", id, device.getClass().getName());
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
    @Nullable
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
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void close() {
        // remove devices which have not been closed by the user
        devices.values().forEach(device -> {
            try {
                logger.atInfo().log("Closing device: {}, {}", device.getConfig().getId(), device.getClass().getName());
                device.close();
            } catch (Exception e) {
                // don't rethrow, just log.
                // we want to attempt to close all devices.
                logger.atError().log("Failed to close device: {}", device, e);
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

    /**
     * Create a proxy for the given instance that intercepts the close method. This ensures that, if a device is closed
     * by the user, the registry is updated to reflect this and a second attempt is not made to close potentially
     * dangling instances on shutdown.
     * <p>
     * This approach would not be required in Kotlin due to language-level support for delegation and composition.
     * <p>
     * On a Raspberry Pi 5, proxying the {@link io.github.iamnicknack.pjs.device.gpio.GpioPort} adds ~30ns (~2%) to
     * each operation over one million operations.
     * <p>
     * Over three million operations this has been observed to drop to ~3ns per operation (0.3%).
     * <p>
     * Either of these figures is negligible compared to the overhead of the GPIO / FFM operations themselves
     * or variation in timing which is incurred by simply running within an operating system.
     *
     * @param instance the instance to create a proxy for
     * @return a proxy instance implementing the determined interface and AutoCloseable
     * @param <T> the type of the instance
     */
    @SuppressWarnings("unchecked")
    private <T extends AutoCloseable> T interceptClose(T instance) {
        // the interface to be represented by the proxy
        var type = switch (instance) {
            case GpioPort _ -> GpioPort.class;
            case Spi _ -> Spi.class;
            case I2C _ -> I2C.class;
            case Pwm _ -> Pwm.class;
            default -> throw new IllegalArgumentException("Unsupported type: " + instance.getClass().getName());
        };

        // a new proxy which explicitly implements the determined interface and AutoCloseable
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { type, AutoCloseable.class },
                new CloseableInvocationHandler<>(instance)
        );
    }

    /**
     * Invocation handler to intercept the close method of a device instance.
     * @param <T> the device type
     */
    private class CloseableInvocationHandler<T extends AutoCloseable> implements InvocationHandler {

        private static final Method DEVICE_CLOSE_METHOD = initCloseMethod();

        private final T instance;

        public CloseableInvocationHandler(T instance) {
            this.instance = instance;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (DEVICE_CLOSE_METHOD.equals(method)) {
                // remove (and close) the device
                DefaultDeviceRegistry.this.remove((Device<?>) instance);
                return null;
            } else {
                try {
                    return method.invoke(instance, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }

        private static Method initCloseMethod() {
            try {
                return Device.class.getMethod("close");
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
