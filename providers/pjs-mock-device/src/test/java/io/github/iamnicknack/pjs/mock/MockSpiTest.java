package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockSpiTest {

    private final SpiConfig config = SpiConfig.builder()
            .id("test-spi")
            .bus(1)
            .build();

    @Test
    void canWriteReadByte() {
        var mock = new MockSpi(config);
        mock.write(42);
        mock.swapBuffers();
        assertThat(mock.read()).isEqualTo(42);
    }

    @Test
    void canWriteReadArray() {
        var mock = new MockSpi(config);
        var bytesOut = new byte[] { 1, 2, 3 };
        mock.writeBytes(bytesOut);
        mock.swapBuffers();

        var bytesIn = mock.readBytes(3);
        assertThat(bytesIn).containsExactly(bytesOut);
    }
}