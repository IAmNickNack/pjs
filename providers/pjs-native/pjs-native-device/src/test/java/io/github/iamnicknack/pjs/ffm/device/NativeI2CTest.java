package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CRegister;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractFileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractIoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CMessage;
import io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CRdwrData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NativeI2CTest {

    @Test
    void canReadI2C() {
        var expectedMessage = new I2CMessage(0x20, 0, 1, new byte[] { 0x01 });
        var ioctlOperations = new AbstractIoctlOperations() {
            @Override
            public <T> T ioctl(int fd, long command, T data) {
                assertThat(data).isInstanceOf(I2CRdwrData.class);
                var rdwrData = (I2CRdwrData) data;
                assertThat(rdwrData.messages()).hasSize(2);
                assertThat(rdwrData.messages()[0]).isEqualTo(expectedMessage);
                rdwrData.messages()[1].buffer()[0] = 0x02;
                return data;
            }
        };

        var fileOperations = new AbstractFileOperations() {
            @Override
            public int close(int fd) {
                return 0;
            }
        };

        var config = I2CConfig.builder().build();
        var fileDescriptor = fileOperations.createFileDescriptor(1);
        try (var i2c = new NativeI2C(config, ioctlOperations, fileDescriptor)) {
            var i2cRegister = new I2CRegister(0x20, 1, i2c);
            var result = i2cRegister.read();
            assertThat(result).isEqualTo(0x02);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    void canWriteI2C() {
        var expectedMessage = new I2CMessage(0x20, 0, 1, new byte[] { 0x02 });
        var ioctlOperations = new AbstractIoctlOperations() {
            @Override
            public <T> T ioctl(int fd, long command, T data) {
                assertThat(data).isInstanceOf(I2CRdwrData.class);
                var rdwrData = (I2CRdwrData) data;
                assertThat(rdwrData.messages()).hasSize(2);
                assertThat(rdwrData.messages()[1]).isEqualTo(expectedMessage);
                return data;
            }
        };

        var fileOperations = new AbstractFileOperations() {
            @Override
            public int close(int fd) {
                return 0;
            }
        };

        var config = I2CConfig.builder().build();
        var fileDescriptor = fileOperations.createFileDescriptor(1);
        try (var i2c = new NativeI2C(config, ioctlOperations, fileDescriptor)) {
            var i2cRegister = new I2CRegister(0x20, 1, i2c);
            i2cRegister.write(0x02);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}