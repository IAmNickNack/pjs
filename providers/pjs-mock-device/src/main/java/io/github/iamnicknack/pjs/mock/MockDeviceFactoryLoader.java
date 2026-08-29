package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.model.device.DeviceFactoryLoader;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class MockDeviceFactoryLoader implements DeviceFactoryLoader<DeviceFactoryLoader.NoConfig> {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return Optional.ofNullable(properties.get("pjs.mode"))
                .filter("mock"::equals)
                .isPresent();
    }

    @Override
    public GenericDeviceFactory load(NoConfig ignored) {
        return new MockDeviceFactory();
    }

    @Override
    public @Nullable GenericDeviceFactory load(Map<String, Object> properties) {
        return load(NoConfig.INSTANCE);
    }
}
