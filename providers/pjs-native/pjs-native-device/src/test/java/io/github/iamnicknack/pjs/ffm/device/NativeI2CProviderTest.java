package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractFileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractIoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CConstants.I2C_FUNC_I2C;
import static org.assertj.core.api.Assertions.assertThat;

class NativeI2CProviderTest {

    @Test
    void canCreateI2C() {
        try (var provider = new NativeI2CProvider(
                new AbstractFileOperations() {
                    @Override
                    public FileDescriptor openFd(String pathname, int flags) {
                        return createFileDescriptor(1);
                    }

                    @Override
                    public int close(FileDescriptor fd) {
                        return 0;
                    }
                },
                new AbstractIoctlOperations() {
                    @Override
                    public int ioctl(int fd, long command, int data) {
                        return I2C_FUNC_I2C;
                    }
                }
        )) {
            var i2c = provider.create(I2CConfig.builder().build());
            assertThat(i2c).isNotNull();
        }
    }

    @Test
    void doesNotCreateI2CWhenRDWRNotSupported() {
        var closeInvoked = new AtomicBoolean(false);
        try (var provider = new NativeI2CProvider(
                new AbstractFileOperations() {
                    @Override
                    public FileDescriptor openFd(String pathname, int flags) {
                        return createFileDescriptor(1);
                    }

                    @Override
                    public int close(int fd) {
                        closeInvoked.set(true);
                        return 0;
                    }
                },
                new AbstractIoctlOperations() {
                    @Override
                    public int ioctl(int fd, long command, int data) {
                        return 0;
                    }
                }
        )) {
            Assertions.assertThrows(IllegalStateException.class, () ->
                    provider.create(I2CConfig.builder().build())
            );
            assertThat(closeInvoked.get()).isTrue();
        }
    }
}