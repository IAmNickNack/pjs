package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public class NativeDeviceRegistryLoader implements DeviceRegistryLoader {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return NativeContext.isAvailable() && Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("ffm"))
                .isPresent();
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        var context = ServiceLoader.load(NativeContext.class, NativeContext.class.getClassLoader()).stream()
                .findFirst()
                .map(ServiceLoader.Provider::get)
                .orElseGet(DefaultNativeContext::new);
        return new NativeDeviceRegistry(context);
    }
}
