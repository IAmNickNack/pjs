package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2CConfigBuilder;
import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link I2CProvider} implementation backed by Pi4J.
 */
public class Pi4jI2CProvider implements I2CProvider {

    /**
     * Pi4J context to use for creating devices.
     */
    private final Context pi4jContext;

    /**
     * Private cache of Pi4J I2C devices.
     */
    private final Map<DeviceCacheKey, com.pi4j.io.i2c.I2C> i2cMap = new HashMap<>();

    /**
     * Constructor.
     * @param pi4jContext the pi4j context to use for creating devices.
     */
    public Pi4jI2CProvider(Context pi4jContext) {
        this.pi4jContext = pi4jContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public I2C create(I2CConfig config) {
        return new Pi4jI2C(config, this);
    }

    /**
     * Construct or retrieve a Pi4J I2C device for the given address.
     * @param address the I2C device address.
     * @param config the PJs configuration for this device.
     * @return the Pi4J I2C device.
     */
    com.pi4j.io.i2c.I2C deviceForAddress(int address, I2CConfig config) {
        var key = new DeviceCacheKey(address, config.getId());
        return i2cMap.computeIfAbsent(key, k -> {
            var pi4jConfig = I2CConfigBuilder.newInstance(pi4jContext)
                    .id(key.toString())
                    .name(key.toString())
                    .bus(config.bus())
                    .device(address)
                    .build();
            return pi4jContext.create(pi4jConfig);
        });
    }

    /**
     * Dispose of all Pi4J I2C devices, removing them from the Pi4J registry.
     */
    @Override
    public void close() {
        i2cMap.keySet().stream()
                .map(DeviceCacheKey::toString)
                .forEach(pi4jContext.registry()::remove);
    }

    /**
     * Unique identifier for a Pi4J I2C device
     * @param i2cDeviceAddress the I2C device address.
     * @param pjsDeviceId the PJs device id.
     */
    private record DeviceCacheKey(int i2cDeviceAddress, String pjsDeviceId) {}
}
