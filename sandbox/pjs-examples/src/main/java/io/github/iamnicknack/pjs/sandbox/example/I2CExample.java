package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.McpI2CRegisterFactory;
import io.github.iamnicknack.pjs.sandbox.example.internal.Mcp23xxxExample;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class I2CExample implements Runnable {

    private static final I2CConfig I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .pin(27)
            .portMode(GpioPortMode.OUTPUT)
            .build();

    private final Mcp23xxxExample example;

    public I2CExample(DeviceRegistry registry) {
        var resetPin = registry.create(RESET_PIN_CONFIG).pin();
        var bus = registry.create(I2C_CONFIG);
        var registerFactory = new McpI2CRegisterFactory(bus, 0x20);
        var device = new Mcp23x08(registerFactory);

        this.example = new Mcp23xxxExample(device, resetPin);
    }

    @Override
    public void run() {
        example.run();
    }
}
