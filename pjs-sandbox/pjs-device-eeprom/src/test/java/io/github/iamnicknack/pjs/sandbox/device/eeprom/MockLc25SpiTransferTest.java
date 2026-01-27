package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.device.spi.impl.CompositeMessage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MockLc25SpiTransferTest {

    @Test
    void calculatePageOffset() {
        var transfer = new MockLc25SpiTransfer(1024, 32);
        assertEquals(0, transfer.pageOffset(0));
        assertEquals(31, transfer.pageOffset(31));
        assertEquals(0, transfer.pageOffset(32));
        assertEquals(31, transfer.pageOffset(63));
    }

    @Test
    void calculatePageAddress() {
        var transfer = new MockLc25SpiTransfer(1024, 32);
        assertEquals(0, transfer.pageAddress(0));
        assertEquals(32, transfer.pageAddress(63));
    }

    @Test
    void readWrapsAroundAddressLimits() {
        var transfer = new MockLc25SpiTransfer(32, 32);
        for (int i = 0; i < 32; i++) {
            transfer.getData()[i] = (byte)i;
        }

        var data = new byte[4];

        var message = CompositeMessage.builder()
                .message(SpiTransfer.Message.write(new byte[] { (byte) Microchip25LcEeprom.Register.READ.value }))
                .message(SpiTransfer.Message.write(new byte[] { 0, 0 }))
                .message(SpiTransfer.Message.read(data))
                .build();

        transfer.transfer(message.write(), 0, message.read(), 0, message.length());
        message.unpack();
        assertThat(data).containsExactly(0, 1, 2, 3);

        message = CompositeMessage.builder()
                .message(SpiTransfer.Message.write(new byte[] { (byte)Microchip25LcEeprom.Register.READ.value }))
                .message(SpiTransfer.Message.write(new byte[] { 0, 30 }))
                .message(SpiTransfer.Message.read(data))
                .build();

        transfer.transfer(message.write(), 0, message.read(), 0, message.length());
        message.unpack();
        assertThat(data).containsExactly(30, 31, 0, 1);


    }

    @TestFactory
    Stream<DynamicTest> writeWrapsAroundPageBoundary() {
        record Expectation(int startAddress, Consumer<byte[]> validator) {}
        Function<Integer, CompositeMessage> messageFactory = startAddress -> CompositeMessage.builder()
                .message(SpiTransfer.Message.write(new byte[] { (byte)Microchip25LcEeprom.Register.WRITE.value }))
                .message(SpiTransfer.Message.write(new byte[] { 0, startAddress.byteValue() }))
                .message(SpiTransfer.Message.write(new byte[] {0x01, 0x02, 0x03, 0x04}))
                .build();

        return Stream.of(
                new Expectation(0, data -> {
                    assertEquals(1, data[0]);
                    assertEquals(2, data[1]);
                    assertEquals(3, data[2]);
                    assertEquals(4, data[3]);
                }),
                new Expectation(29, data -> {
                    assertEquals(1, data[29]);
                    assertEquals(2, data[30]);
                    assertEquals(3, data[31]);
                    assertEquals(4, data[0]);
                }),
                new Expectation(60, data -> {
                    assertEquals(1, data[60]);
                    assertEquals(2, data[61]);
                    assertEquals(3, data[62]);
                    assertEquals(4, data[63]);
                }),
                new Expectation(61, data -> {
                    assertEquals(1, data[61]);
                    assertEquals(2, data[62]);
                    assertEquals(3, data[63]);
                    assertEquals(4, data[32]);
                })
        ).map(e -> DynamicTest.dynamicTest("start address " + e.startAddress, () -> {
            var message = messageFactory.apply(e.startAddress);
            var transfer = new MockLc25SpiTransfer(64, 32);
            transfer.write(Microchip25LcEeprom.Register.WREN.value);
            var length = transfer.transfer(message.write(), 0, new byte[0], 0, message.length());
            assertEquals(message.length() - 3, length);

            e.validator.accept(transfer.getData());
        }));
    }
}