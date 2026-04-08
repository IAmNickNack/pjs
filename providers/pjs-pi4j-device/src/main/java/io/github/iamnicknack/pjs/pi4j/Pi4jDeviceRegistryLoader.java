package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.context.ContextBuilder;
import com.pi4j.extension.Plugin;
import com.pi4j.extension.PluginService;
import com.pi4j.platform.Platform;
import com.pi4j.provider.Provider;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Pi4jDeviceRegistryLoader implements DeviceRegistryLoader<Pi4jDeviceRegistryLoader.Config> {

    @Nullable
    private final Class<? extends Plugin> preferredPlugin;

    public Pi4jDeviceRegistryLoader() {
        this(null);
    }

    public Pi4jDeviceRegistryLoader(@Nullable Class<? extends Plugin> preferredPlugin) {
        this.preferredPlugin = preferredPlugin;
    }

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("pi4j"))
                .isPresent();
    }

    @Override
    public DeviceRegistry load(Config registryConfig) {
        var javaProperties = new HashMap<>(registryConfig.javaProperties());

        if (registryConfig.grpcHost() != null) {
            javaProperties.put("pi4j.grpc.host", registryConfig.grpcHost());
            javaProperties.put("pi4j.grpc.port", String.valueOf(registryConfig.grpcPort));
        }

        if (preferredPlugin != null) {
            var contextBuilder = Pi4J.newContextBuilder().properties(javaProperties);
            var plugin = pluginInstance(preferredPlugin);
            plugin.initialize(new CustomPluginService(contextBuilder, javaProperties));
            return new Pi4jDeviceRegistry(contextBuilder.build());
        } else {
            return new Pi4jDeviceRegistry(Pi4J.newContextBuilder()
                    .properties(javaProperties)
                    .autoDetect()
                    .build()
            );
        }
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        return load(new Config(properties));
    }

    /**
     * Helper function to instantiate a plugin instance via reflection and map exceptions to runtime exceptions.
     * @param clazz the plugin class to instantiate
     * @return the instantiated plugin instance
     * @param <T> the plugin type
     */
    private static <T extends Plugin> T pluginInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Workaround to get PI4J autoconfiguration to load a specific {@link Plugin} instance
     */
    private static class CustomPluginService implements PluginService {
        private final ContextBuilder contextBuilder;
        private final Context propertiesContext;

        /**
         * Constructor.
         * @param contextBuilder the context builder currently being used to create the pi4j context.
         * @param properties properties to inject into a temporary context which will be passed
         *                   to {@link Plugin#initialize(PluginService)}.
         */
        private CustomPluginService(ContextBuilder contextBuilder, Map<String, String> properties) {
            this.contextBuilder = contextBuilder;
            this.propertiesContext = Pi4J.newContextBuilder().properties(properties).build();
        }

        @Override
        public Context context() {
            return propertiesContext;
        }

        @Override
        public PluginService register(Provider... provider) {
            for (var p : provider) {
                contextBuilder.add(p);
            }
            return this;
        }

        @Override
        public PluginService register(Platform... platform) {
            for (var p : platform) {
                contextBuilder.addPlatform(p);
            }
            return this;
        }
    }

    public record Config(
            @Nullable String grpcHost,
            int grpcPort,
            Map<String, String> javaProperties
    ) {
        Config(Map<String, Object> properties) {
            this(
                    (String) properties.get("pi4j.grpc.host"),
                    (Integer) properties.get("pi4j.grpc.port"),
                    properties.entrySet().stream()
                            .filter(e -> e.getValue() != null)
                            .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }
}
