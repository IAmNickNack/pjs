package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.McpSpiTransferRegister;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.sandbox.example.internal.Mcp23xxxExample;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;

public class SpiExample implements Runnable {

    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .bus(1)
            .chipSelect(1)
            .baudRate(1_000_000)
            .build();

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .pin(27)
            .mode(GpioPortMode.OUTPUT)
            .build();


    private final Mcp23xxxExample example;

    public SpiExample(DeviceRegistry registry) {
        var resetPin = registry.create(RESET_PIN_CONFIG).pin();
        var spi = registry.create(SPI_CONFIG);
//        var registerFactory = new Mcp23xxxRegisterFactory.McpSpiRegisterFactory(spi);
//        var registerFactory = new McpSpiTransferRegisterFactory(spi);
        var provider = (SpiProvider)registry.getProvider(SpiConfig.class);
        var transfer = provider.createTransfer(spi);
        var registerFactory = new McpSpiTransferRegister.Factory(transfer);
        var device = new Mcp23x08(registerFactory);

        this.example = new Mcp23xxxExample(device, resetPin);
    }

    @Override
    public void run() {
        this.example.run();
    }
}
