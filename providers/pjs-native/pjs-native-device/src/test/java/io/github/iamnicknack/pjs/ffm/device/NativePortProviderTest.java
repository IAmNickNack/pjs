package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractFileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractIoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.ChipInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineRequest;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.PinFlag;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.EventPollerFactoryImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NativePortProviderTest {

    private final Logger logger = LoggerFactory.getLogger(NativePortProviderTest.class);

    private final FileOperations fileOperations = new AbstractFileOperations() {
        @Override
        public FileDescriptor openFd(String pathname, int flags) {
            return this.createFileDescriptor(1);
        }
    };

    private final EventPoller.Factory eventPollerFactory = new EventPollerFactoryImpl(
            Duration.ofMillis(100),
            (poll, _) -> poll,
            fileOperations
    );

    @Test
    public void defaultsToInputPort() {
        var ioctlOperations = AbstractIoctlOperations.builder()
                .addHandler(GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL)
                .addHandler(GpioConstants.GPIO_V2_GET_LINE_IOCTL, (fd, _, data) -> {
                    logger.info("Getting line for pin {}", data);
                    assertThat(fd).as("File descriptor is configured").isEqualTo(1);

                    var lineRequest = (LineRequest)data;
                    assertThat(lineRequest.offsets()).as("Pin matches").containsExactly(1);
                    assertThat(lineRequest.config().flags()).as("Input is set").isEqualTo(PinFlag.INPUT.value);
                    assertThat(lineRequest.config().attributes()).as("No additional attributes").isEmpty();

                    return data;
                })
                .build();

        try (var provider = new NativePortProvider(
                new ChipInfo("test", "test", 1),
                fileOperations,
                ioctlOperations,
                eventPollerFactory
        )) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = provider.create(config);
            assertThat(device).isNotNull();
        }
    }

    @Test
    public void failsIfLineIsInUse() {
        var invocationCount = new AtomicInteger(0);
        var ioctlOperations = AbstractIoctlOperations.builder()
                .addHandler(GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL, (fd, _, data) -> {
                    invocationCount.incrementAndGet();
                    assertThat(fd).as("File descriptor is configured").isEqualTo(1);
                    var lineInfo = (LineInfo)data;
                    return (lineInfo.offset() == 1)
                            ? data
                            : new LineInfo(
                            lineInfo.name(),
                            lineInfo.consumer(),
                            lineInfo.offset(),
                            PinFlag.USED.value,
                            lineInfo.attrs()
                    );
                })
                .build();

        try (var provider = new NativePortProvider(
                new ChipInfo("test", "test", 1),
                fileOperations,
                ioctlOperations,
                eventPollerFactory
        )) {
            var config = GpioPortConfig.builder().pin(1, 2).build();
            assertThatThrownBy(() -> provider.create(config)).isInstanceOf(IllegalStateException.class);
            assertThat(invocationCount.get()).isEqualTo(2);
        }
    }

    @Test
    public void doesNotEnableEventSupport() {
        var ioctlOperations = AbstractIoctlOperations.builder()
                .addHandler(GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL)
                .addHandler(GpioConstants.GPIO_V2_GET_LINE_IOCTL)
                .build();

        try (var provider = new NativePortProvider(
                new ChipInfo("test", "test", 1),
                fileOperations,
                ioctlOperations,
                eventPollerFactory
        )) {
            var config = GpioPortConfig.builder().pin(1).build();
            var device = (NativePort)provider.create(config);
            assertThat(device).isNotNull();
        }
    }

}