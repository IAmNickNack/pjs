package io.github.iamnicknack.pjs.device.spi.impl;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.mock.MockSpi;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultSpiTransferTest {

    @Test
    void canWriteReadSingleMessage() {
        var spi = new MockSpi(SpiConfig.builder().id("test-spi").build());
        var transfer = new DefaultSpiTransfer(spi);

        transfer.transfer(SpiTransfer.Message.write(new byte[] { 1, 2, 3 }));
        spi.swapBuffers();
        var bytes = new byte[3];
        transfer.transfer(SpiTransfer.Message.read(bytes));

        assertArrayEquals(new byte[] { 1, 2, 3 }, bytes);
    }

    @Test
    void canWriteThenReadMultipleMessages() {
        var spi = new MockSpi(SpiConfig.builder().id("test-spi").build());

        // put data in the buffer
        spi.writeBytes(new byte[] { 0, 0, 0, 4, 5, 6 });
        spi.swapBuffers();

        var transfer = new DefaultSpiTransfer(spi);

        // read the last 3 bytes
        var bytes = new byte[3];
        transfer.transfer(
                SpiTransfer.Message.write(new byte[] { 1, 2, 3 }),
                SpiTransfer.Message.read(bytes)
        );

        assertArrayEquals(new byte[] { 4, 5, 6 }, bytes);
    }

    @Test
    void doesntFailWithNoMessages() {
        var spi = new MockSpi(SpiConfig.builder().id("test-spi").build());
        var transfer = new DefaultSpiTransfer(spi);

        transfer.transfer();

        assertTrue(true);
    }

    @TestFactory
    Stream<DynamicTest> invocationChecks() {
        record Expectation(boolean single, boolean multi, SpiTransfer.Message... messages) {}

        var message = SpiTransfer.Message.write(new byte[0]);

        return Stream.of(
                new Expectation(true, false, message),
                new Expectation(false, true, message, message),
                new Expectation(false, true)
        ).map(e -> DynamicTest.dynamicTest(
                String.format("single=%s, multi=%s, messages=%d", e.single, e.multi, e.messages.length),
                () -> {
                    var singleInvocation = new InvocationCheckTransfer();
                    var multiInvocation = new InvocationCheckTransfer();
                    var transfer = new DefaultSpiTransfer(singleInvocation, multiInvocation);
                    transfer.transfer(e.messages);
                    assertEquals(e.single, singleInvocation.invoked.get());
                    assertEquals(e.multi, multiInvocation.invoked.get());
                }
        ));
    }

    @Test
    void compositeMessageTransferCanExecuteSingleTransfer() {
        var spi = new MockSpi(SpiConfig.builder().id("test-spi").build());
        spi.writeBytes(new byte[] { 0, 0, 0, 7, 8, 9, 0, 0, 0 });
        spi.swapBuffers();

        var countingSpi = new CountingSpi(spi);

        var transfer = new CompositeMessageTransfer(countingSpi);

        var messages = new SpiTransfer.Message[] {
                SpiTransfer.Message.write(new byte[] { 1, 2, 3 }),
                SpiTransfer.Message.read(new byte[3]),
                SpiTransfer.Message.write(new byte[] { 4, 5, 6 }),
        };

        int length = transfer.transfer(messages);
        assertThat(length).isEqualTo(9);
        assertThat(messages[1].read()).containsExactly(7, 8, 9);
        assertThat(countingSpi.getCount()).isEqualTo(1);

        var bytes = new byte[9];
        spi.getOutBuffer().get(0, bytes, 0, 9);
        assertThat(bytes).containsExactly(1, 2, 3, 0, 0, 0, 4, 5, 6);
    }

    @Test
    void compositeMessageTransferCanExecuteMultipleTransfers() {
        var spi = new MockSpi(SpiConfig.builder().id("test-spi").build());
        spi.writeBytes(new byte[] { 0, 0, 0, 7, 8, 9, 0, 0, 0 });
        spi.swapBuffers();

        var countingSpi = new CountingSpi(spi);

        var transfer = new CompositeMessageTransfer(countingSpi);

        var messages = new SpiTransfer.Message[] {
                new SpiTransfer.DefaultMessage(new byte[] { 1, 2, 3 }, 0, new byte[3], 0, 3, 0, false),
                new SpiTransfer.DefaultMessage(new byte[3], 0, new byte[3], 0, 3, 0, true),
                new SpiTransfer.DefaultMessage(new byte[] { 4, 5, 6 }, 0, new byte[3], 0, 3, 0, false),
        };

        int length = transfer.transfer(messages);
        assertThat(length).isEqualTo(9);
        assertThat(messages[1].read()).containsExactly(7, 8, 9);
        assertThat(countingSpi.getCount()).isEqualTo(2);

        var bytes = new byte[9];
        spi.getOutBuffer().get(0, bytes, 0, 9);
        assertThat(bytes).containsExactly(1, 2, 3, 0, 0, 0, 4, 5, 6);
    }

    private static class InvocationCheckTransfer implements SpiTransfer {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        @Override
        public int transfer(Message... messages) {
            invoked.set(true);
            return 0;
        }
    }

    private static class CountingSpi implements Spi {
        private final Spi delegate;
        private int count;

        public CountingSpi(Spi delegate) {
            this.delegate = delegate;
        }

        @Override
        public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
            count++;
            return delegate.transfer(write, writeOffset, read, readOffset, length);
        }

        public int getCount() {
            return count;
        }

        @Override
        public DeviceConfig<Spi> getConfig() {
            return delegate.getConfig();
        }
    }
}