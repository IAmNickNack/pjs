package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.device.spi.impl.DefaultSpiTransfer;
import io.github.iamnicknack.pjs.logging.LoggingSpiTransfer;
import io.github.iamnicknack.pjs.mock.MockGpioPort;
import io.github.iamnicknack.pjs.mock.MockSpi;
import io.github.iamnicknack.pjs.model.pin.Pin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Microchip25LcEepromTest {

    private final MockSpi spi = new MockSpi(SpiConfig.builder().id("test-spi").build(), 256);
    private final SpiTransfer spiTransfer = new DefaultSpiTransfer(spi);
    private final Pin hold = new MockGpioPort(GpioPortConfig.builder().id("test-port").pin(0).build()).pin();

    @Test
    void writesSpiData() {
        var eeprom = new Microchip25LcEeprom(new LoggingSpiTransfer(spiTransfer, "spi-transfer"), hold);
        eeprom.write(0x100, new byte[] { 3, 2, 1 });

        var buffer = spi.getOutBuffer();
        var bytesOut = new byte[buffer.position()];
        buffer.get(0, bytesOut, 0, bytesOut.length);
        assertThat(bytesOut).containsExactly(new byte[] { 4, 6, 2, 1, 0, 3, 2, 1, 5, 0, 4 });
    }

    @Test
    void writesMultiplePages() {
        var eeprom = new Microchip25LcEeprom(
                new LoggingSpiTransfer(spiTransfer, "spi-transfer"),
                hold,
                new PageFunction.DefaultPageFunction(32)
        );
        var data = new byte[33];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        eeprom.write(32, data);

        var buffer = spi.getOutBuffer();
        var bytesOut = new byte[buffer.position()];
        buffer.get(0, bytesOut, 0, bytesOut.length);

        System.out.println(Arrays.toString(bytesOut));

        assertThat(bytesOut).hasSize(48);
        // first write operation
        assertThat(bytesOut[0]).isEqualTo((byte) 4); // write disable during constructor
        assertThat(bytesOut[1]).isEqualTo((byte) 6); // write enable for page write
        assertThat(bytesOut[2]).isEqualTo((byte) 2); // write register
        assertThat(bytesOut[36]).isEqualTo((byte) 31); // last data byte of first page
        assertThat(bytesOut[37]).isEqualTo((byte) 5); // read status register
        assertThat(bytesOut[38]).isEqualTo((byte) 0); // ???
        assertThat(bytesOut[39]).isEqualTo((byte) 4); // write disable after page writes
        // second write operation
        assertThat(bytesOut[40]).isEqualTo((byte) 6); // write enable
        assertThat(bytesOut[41]).isEqualTo((byte) 2); // write register
        assertThat(bytesOut[42]).isEqualTo((byte) 0); // address high
        assertThat(bytesOut[43]).isEqualTo((byte) 64); // address low
        assertThat(bytesOut[44]).isEqualTo((byte) 32); // last data byte
        assertThat(bytesOut[45]).isEqualTo((byte) 5); // read status register
        assertThat(bytesOut[46]).isEqualTo((byte) 0); // ???
        assertThat(bytesOut[47]).isEqualTo((byte) 4); // write disable after page writes
    }

    @TestFactory
    Stream<DynamicTest> writeWrapsAroundPageBoundary() {
        record Expectation(int startAddress, Consumer<byte[]> validator) {}

        return Stream.of(0, 29, 60, 61)
                .map(startAddress -> DynamicTest.dynamicTest("start address " + startAddress, () -> {
                    var transfer = new LoggingSpiTransfer(new MockLc25SpiTransfer(64, 32), "transfer");
                    var eeprom = new Microchip25LcEeprom(transfer, hold);

                    eeprom.write(startAddress, new byte[] { 1, 2, 3, 4 });
                    var data = eeprom.read(startAddress, 4);

                    assertEquals(1, data[0]);
                    assertEquals(2, data[1]);
                    assertEquals(3, data[2]);
                    assertEquals(4, data[3]);
                }));
    }

}