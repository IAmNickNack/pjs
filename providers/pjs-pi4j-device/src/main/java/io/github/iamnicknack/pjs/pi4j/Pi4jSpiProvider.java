package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import com.pi4j.io.spi.SpiConfigBuilder;
import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;

import java.util.HashMap;
import java.util.Map;

public class Pi4jSpiProvider implements SpiProvider {

    private final Context pi4jContext;
    private final Map<String, String> deviceIdMap = new HashMap<>();

    public Pi4jSpiProvider(Context pi4jContext) {
        this.pi4jContext = pi4jContext;
    }

    @Override
    public Spi create(SpiConfig config) {
        com.pi4j.io.spi.SpiConfig pi4jSpiConfig = SpiConfigBuilder.newInstance(pi4jContext)
                .name(config.getId())
                .bus(config.bus())
                .channel(config.chipSelect())
                .baud(config.baudRate())
                .mode(config.mode())
                .build();

        com.pi4j.io.spi.Spi pi4jSpi = pi4jContext.create(pi4jSpiConfig);
        deviceIdMap.put(config.getId(), pi4jSpi.getId());

        return new Pi4jSpi(
                config,
                pi4jSpi,
                () -> pi4jContext.registry().remove(deviceIdMap.get(config.getId()))
        );
    }
}
