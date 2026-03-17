package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.McpSpiTransferRegister;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiTransferProvider;
import io.github.iamnicknack.pjs.impl.DefaultPinOperations;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08.IODIR;
import static io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08.OLAT;

public class McpInterruptExample implements Runnable {
    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .bus(1)
            .chipSelect(1)
            .baudRate(1_000_000)
            .build();

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .id("RESET-PIN")
            .pin(27)
            .portMode(GpioPortMode.OUTPUT)
            .defaultValue(1)
            .build();

    private static final GpioPortConfig INTERRUPT_PIN_CONFIG = GpioPortConfig.builder()
            .id("INTERRUPT-PIN")
            .pin(14)
            .portMode(GpioPortMode.INPUT_PULLUP)
            .build();

    private static final GpioPortConfig OUTPUT_PIN_CONFIG = GpioPortConfig.builder()
            .id("OUTPUT-PIN")
            .pin(1)
            .portMode(GpioPortMode.OUTPUT)
            .defaultValue(0)
            .build();

    private final Logger logger = LoggerFactory.getLogger(McpInterruptExample.class);
    private final DeviceRegistry deviceRegistry;
    private final SpiTransferProvider spiTransferProvider;

    public McpInterruptExample(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.spiTransferProvider = (SpiTransferProvider)deviceRegistry.getProvider(SpiConfig.class);

        logger.info("SPI transfer provider: {}", spiTransferProvider.getClass().getSimpleName());
    }

    public void run() {
        var resetPin = new DefaultPinOperations(deviceRegistry.create(RESET_PIN_CONFIG).pin());
        var outputPin = new DefaultPinOperations(deviceRegistry.create(OUTPUT_PIN_CONFIG).pin());
        var interruptPort = deviceRegistry.create(INTERRUPT_PIN_CONFIG);
        var spi = deviceRegistry.create(SPI_CONFIG);
        var spiTransfer = spiTransferProvider.createTransfer(spi);

        try(var mcp = new Mcp23x08(new McpSpiTransferRegister.Factory(spiTransfer), interruptPort)) {
            resetPin.pulse();

            var state = mcp.read();
            logger.info("MCP23008 start state: {}", state);

            // performs an atomic update of the MCP23x08 registers
            var buffer = state.buffer();
            buffer[IODIR] = 0x01;
            buffer[OLAT] = 0x54;
            mcp.write(new Mcp23x08.State(buffer));
//
//            // non-atomic registers update
//            mcp.iodir.write(0x01);
//            mcp.olat.write(0x54);
//
            var latch = new CountDownLatch(1);
            mcp.interruptOnChange(0x01, 0, event -> {
                logger.info("Got event: {}, 0x{}", event, Integer.toHexString(event.port().intcap.read()));
                latch.countDown();
            });
//
            Thread.sleep(1000);
            outputPin.pulse();

            if (latch.await(5000, TimeUnit.MILLISECONDS)) {
                logger.info("MCP23x08 end state: {}", mcp.read());
            } else {
                logger.warn("Event timed out");
            }

            logger.info("#");
            logger.info("#");
            logger.info("#");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        resetPin.low();
    }
}
