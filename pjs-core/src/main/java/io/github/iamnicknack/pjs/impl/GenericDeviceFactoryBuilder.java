package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class GenericDeviceFactoryBuilder {

    /**
     * A {@link Supplier} for the root {@link GenericDeviceFactory}
     */
    @Nullable
    private Supplier<? extends GenericDeviceFactory> deviceRegistrySupplier;

    /**
     * A list of decorator functions to be applied to the root {@link GenericDeviceFactory}
     */
    private final List<UnaryOperator<GenericDeviceFactory>> decorators = new ArrayList<>();

    /**
     * {@link DeviceFactory} instances which will be used to form the root factory
     */
    private final Map<Class<? extends DeviceConfig<?>>, DeviceFactory<?, ?>> factoryMap = new HashMap<>();

    /**
     * Add a {@link DeviceFactory} if building a root factory.
     * @param factory the factory to add
     * @param configClass the config class for the factory
     * @return this builder
     * @param <T> the device type
     * @param <V> the config type
     */
    public final <T extends Device<T>, V extends DeviceConfig<T>> GenericDeviceFactoryBuilder factory(
            DeviceFactory<T, V> factory,
            Class<V> configClass
    ) {
        if (factoryMap.containsKey(configClass)) {
            throw new IllegalArgumentException("Factory already registered for config class: " + configClass);
        }
        if (deviceRegistrySupplier != null) {
            throw new IllegalArgumentException("Factory already provided");
        }
        factoryMap.put(configClass, factory);
        return this;
    }

    /**
     * Provide a factory method to for the {@link GenericDeviceFactory} which will be used to create the
     * root factory instance, providing the devices which will perform actual IO operations.
     * @param deviceRegistrySupplier the supplier to create the root factory instance
     * @return this builder instance
     */
    public GenericDeviceFactoryBuilder factory(Supplier<? extends GenericDeviceFactory> deviceRegistrySupplier)  {
        if (this.deviceRegistrySupplier != null) {
            throw new IllegalArgumentException("Factory already provided");
        }
        if (!this.factoryMap.isEmpty()) {
            throw new IllegalArgumentException("Factories already provided");
        }
        this.deviceRegistrySupplier = deviceRegistrySupplier;
        return this;
    }

    /**
     * Add a decorator to the factory. The decorator will add additional functionality to the root factory.
     * <p>
     * Multiple decorators can be provided, and they will be applied in the order in which they are added.
     *
     * @param decorator the decorator function to apply
     * @return this builder instance
     */
    public GenericDeviceFactoryBuilder decorator(UnaryOperator<GenericDeviceFactory> decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public GenericDeviceFactory build() {
        var rootFactory = (this.deviceRegistrySupplier != null)
                ? deviceRegistrySupplier.get()
                : new Factory();

        for (var decorator : decorators) {
            rootFactory = decorator.apply(rootFactory);
        }
        return rootFactory;
    }

    /**
     * A {@link GenericDeviceFactory} constructed from provided {@link DeviceFactory} instances.
     */
    private class Factory implements GenericDeviceFactory {
        private Factory() {
            if (factoryMap.isEmpty()) {
                throw new IllegalArgumentException("No factory registered");
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
            var factory = (DeviceFactory<T, DeviceConfig<T>>) factoryMap.get(config.getClass());
            if (factory == null) {
                throw new IllegalArgumentException("No factory registered for config type: " + config.getClass());
            }
            return factory.create(config);
        }

    }
}
