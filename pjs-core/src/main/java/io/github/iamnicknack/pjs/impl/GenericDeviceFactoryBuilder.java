package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;

import java.util.HashMap;
import java.util.Map;

public class GenericDeviceFactoryBuilder {
    private final Map<Class<? extends DeviceConfig<?>>, DeviceFactory<?, ?>> factoryMap = new HashMap<>();

    public final <T extends Device<T>, V extends DeviceConfig<T>> GenericDeviceFactoryBuilder factory(
            DeviceFactory<T, V> factory,
            Class<V> configClass
    ) {
        factoryMap.put(configClass, factory);
        return this;
    }

    @SuppressWarnings("unchecked")
    public GenericDeviceFactory build() {
        return new GenericDeviceFactory() {
            @Override
            public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
                var factory = (DeviceFactory<T, DeviceConfig<T>>) factoryMap.get(config.getClass());
                if (factory == null) {
                    throw new DeviceRegistry.RegistryException(
                            "No factory registered for config type: " + config.getClass(),
                            new IllegalArgumentException()
                    );
                }
                return factory.create(config);
            }
        };
    }
}
