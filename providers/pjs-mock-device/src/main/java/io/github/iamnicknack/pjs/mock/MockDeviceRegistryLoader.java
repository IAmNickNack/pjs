package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class MockDeviceRegistryLoader implements DeviceRegistryLoader<DeviceRegistryLoader.NoConfig> {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("mock"))
                .isPresent();
    }

    @Override
    public DeviceRegistry load(NoConfig ignored) {
        return new MockDeviceRegistry();
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        return load(NoConfig.INSTANCE);
    }
}
