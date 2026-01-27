package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.sandbox.device.eeprom.Microchip25LcEeprom;
import io.github.iamnicknack.pjs.sandbox.device.eeprom.PageFunction;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EepromExample implements Runnable {

    /**
     * The page size of the EEPROM, according to the datasheet, is 32 bytes.
     */
    private static final int PAGE_SIZE = 32;

    /**
     * Intentionally allocate one extra byte to show that the last byte is not overwritten
     * by the {@link PageFunction} used to
     * calculate the writeable regions of the EEPROM.
     */
    private static final int BUFFER_SIZE = PAGE_SIZE + 1;

    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .bus(1)
            .chipSelect(1)
            .baudRate(100_000)
            .build();

    private static final GpioPortConfig HOLD_PIN_CONFIG = GpioPortConfig.builder()
            .id("HOLD-PIN")
            .pin(27)
            .mode(GpioPortMode.OUTPUT)
            .build();

    private final Logger logger = LoggerFactory.getLogger(EepromExample.class);

    private final Microchip25LcEeprom eeprom;

    public EepromExample(DeviceRegistry registry) {
        var holdPin = registry.create(HOLD_PIN_CONFIG).pin();

        var spi = registry.create(SPI_CONFIG);
        var spiTransfer = ((SpiProvider)registry.getProvider(SpiConfig.class)).createTransfer(spi);
        eeprom = new Microchip25LcEeprom(spiTransfer, holdPin, new PageFunction.DefaultPageFunction(PAGE_SIZE));
    }

    @Override
    public void run() {
        eeprom.setBlockProtection(Microchip25LcEeprom.BlockProtection.NONE);

        var address = (int)(Math.random() * (0x2000 - BUFFER_SIZE));
        logger.info("Writing to address {}", String.format("0x%04X", address));

        var data = new byte[BUFFER_SIZE];
        for (int i = 0; i < data.length; i++) {
            data[i] = ((byte) (0xff^i));
        }
        eeprom.write(address, data);

//        var pageData = eeprom.pageData(address);
//        logger.info("Page data at address {}: {}", String.format("0x%04X", address), Arrays.toString(pageData.data()));

        var buffer = eeprom.read(address, BUFFER_SIZE);
        logger.info("Read {} bytes: {}", buffer.length, Arrays.toString(buffer));

        assert Arrays.equals(data, buffer);
    }

//    /**
//     * Used to help find the size of an eeprom.
//     */
//    @Override
//    public void run() {
//        int[] addresses = new int[] { 0x0000, 0x1000, 0x2000, 0x3000, 0x4000 };
//        for (int address : addresses) {
//            eeprom.write(address, new byte[] { (byte)(address >> 8) });
//        }
//        for (int address : addresses) {
//            int value = eeprom.read(address, 1)[0];
//            System.out.printf("0x%04X: 0x%02x%n", address, value);
//        }
//    }
}
