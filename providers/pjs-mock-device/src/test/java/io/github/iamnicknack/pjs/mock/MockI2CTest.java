package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CRegister;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CSerialPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockI2CTest {

    private final I2CConfig config = I2CConfig.builder()
            .id("test-i2c")
            .bus(1)
            .build();

    @Test
    void canWrite() {
        var device = new MockI2C(config, 256);
        var message = new I2C.Message(0x20, new byte[] { 1, 2, 3 }, 0, 3, I2C.Message.Type.WRITE);
        device.transfer(new I2C.Message[] { message });

        device.getDeviceBuffer().flip();
        assertThat(device.getDeviceBuffer().get()).isEqualTo((byte)1);
        assertThat(device.getDeviceBuffer().get()).isEqualTo((byte)2);
        assertThat(device.getDeviceBuffer().get()).isEqualTo((byte)3);
    }

    @Test
    void canWriteReadDevice() {
        var bus = new MockI2C(config, 256);
        var device = new I2CSerialPort(0x20, bus);

        device.writeBytes(new byte[] { 1, 2, 3 });

        bus.getDeviceBuffer().flip();
        assertThat(bus.getDeviceBuffer().get()).isEqualTo((byte)1);
        assertThat(bus.getDeviceBuffer().get()).isEqualTo((byte)2);
        assertThat(bus.getDeviceBuffer().get()).isEqualTo((byte)3);

        bus.getDeviceBuffer().flip();
        var bytesIn = device.readBytes(3);
        assertThat(bytesIn).containsExactly(new byte[] { 1, 2, 3 });
    }

    @Test
    void canWriteReadRegister() {
        var bus = new MockI2C(config, 256);
        var device = new I2CRegister(0x20, 1, bus);

        device.writeBytes(new byte[] { 1, 2, 3 });

        var buffer = bus.getBuffer(1);

        buffer.flip();
        assertThat(buffer.get()).isEqualTo((byte)1);
        assertThat(buffer.get()).isEqualTo((byte)2);
        assertThat(buffer.get()).isEqualTo((byte)3);

        buffer.flip();
        var bytesIn = device.readBytes(3);
        assertThat(bytesIn).containsExactly(new byte[] { 1, 2, 3 });
    }

}