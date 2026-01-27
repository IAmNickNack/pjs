package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.Arena;
import java.util.Map;
import java.util.Optional;

public class NativeDeviceRegistryLoader implements DeviceRegistryLoader {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return NativeContext.isAvailable() && Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("ffm"))
                .isPresent();
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        return new NativeDeviceRegistry(new NativeContext(Arena.ofAuto()));
    }
}
