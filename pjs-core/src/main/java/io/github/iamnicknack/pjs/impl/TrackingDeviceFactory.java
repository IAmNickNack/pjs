package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class TrackingDeviceFactory implements DeviceRegistry {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GenericDeviceFactory delegate;
    private final Map<String, Device<?>> devices = new HashMap<>();

    public TrackingDeviceFactory(GenericDeviceFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        var device = interceptClose(delegate.create(config));
        devices.put(config.getId(), device);
        logger.atInfo().log("Created {} device with id: {}", device.getClass().getSimpleName(), config.getId());
        return device;
    }

    @Override
    public void close() {
        new ArrayList<>(this.devices.values()).forEach(device -> {
            try {
                logger.atInfo().log("Closing device: {}, {}", device.getConfig().getId(), device.getClass().getName());
                device.close();
            } catch (Exception e) {
                logger.atError().log("Failed to close device: {}", device, e);
            }
        });
    }

    @Override
    public Iterator<Device<?>> iterator() {
        return devices.values().iterator();
    }

    @Override
    public @Nullable <T extends Device<T>> T device(String id, Class<T> deviceType) {
        var device = devices.get(id);
        if (device == null) {
            return null;
        }
        if (deviceType.isInstance(device)) {
            return deviceType.cast(device);
        }
        throw new IllegalArgumentException("Device with id " + id + " is not of type " + deviceType.getName());
    }

    @Override
    public boolean contains(String id) {
        return devices.containsKey(id);
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
    private <T extends Device<?>> T interceptClose(T instance) {
        // the interface to be represented by the proxy
        var type = switch (instance) {
            case GpioPort _ -> GpioPort.class;
            case Spi _ -> Spi.class;
            case I2C _ -> I2C.class;
            case Pwm _ -> Pwm.class;
            default -> throw new IllegalArgumentException("Unsupported type: " + instance.getClass().getName());
        };

        var interfaces = Stream.concat(
                Stream.of(type),
                Arrays.stream(instance.getClass().getInterfaces())
        ).distinct().toArray(Class<?>[]::new);

        // a new proxy which explicitly implements the determined interface and AutoCloseable
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                interfaces,
                new TrackingDeviceFactory.CloseableInvocationHandler<>(instance)
        );
    }

    /**
     * Invocation handler to intercept the close method of a device instance.
     * @param <T> the device type
     */
    private class CloseableInvocationHandler<T extends Device<?>> implements InvocationHandler {

        private static final Method DEVICE_CLOSE_METHOD = initCloseMethod();

        private final T instance;

        public CloseableInvocationHandler(T instance) {
            this.instance = instance;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (DEVICE_CLOSE_METHOD.equals(method)) {
                // remove the device from tracking
                var device = TrackingDeviceFactory.this.devices.remove(instance.getConfig().getId());
                logger.info("{}, Method: {}", delegate.getClass(), method);
                if (device != null) {
                    try {
                        instance.close();
                    } catch (Exception e) {
                        logger.atError().log("Failed to close device: {}", device, e);
                    }
                }
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
