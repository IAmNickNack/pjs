package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.Pi4J;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Pi4jDeviceRegistryLoader implements DeviceRegistryLoader {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("pi4j"))
                .isPresent();
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        var javaProperties = properties.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var pi4j = Pi4J.newContextBuilder()
                .properties(javaProperties)
                .autoDetect()
                .build();

        return new Pi4jDeviceRegistry(pi4j);
    }
}
