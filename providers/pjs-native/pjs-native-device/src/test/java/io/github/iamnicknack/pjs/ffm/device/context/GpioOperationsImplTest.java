package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.ChipInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineAttribute;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineInfo;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class GpioOperationsImplTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canReadChipInfo() {
        performTest(
                builder -> defaultOperations("canReadChipInfo").apply(builder)
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            assertThat(args[0]).isEqualTo(1);
                            assertThat(args[1]).isEqualTo(GpioConstants.GPIO_GET_CHIPINFO_IOCTL);
                            var segment = (MemorySegment)args[2];
                            var chipInfoSegment = mapper.segment(new ChipInfo("chip name", "chip label", 3));
                            segment.copyFrom(chipInfoSegment);
                            return 0;
                        }),
                gpioOperations -> assertThat(gpioOperations.chipInfo("canReadChipInfo"))
                        .isEqualTo(new ChipInfo("chip name", "chip label", 3))
        );
    }

    @Test
    void canReaLines() {
        performTest(
                builder -> defaultOperations("/dev/chip name").apply(builder)
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            assertThat(args[0]).isEqualTo(1);
                            assertThat(args[1]).isEqualTo(GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL);
                            var segment = (MemorySegment)args[2];
                            var segmentValue =  mapper.value(segment, LineInfo.class);
                            var lineInfo = new LineInfo("line " + segmentValue.offset(), "Test", segmentValue.offset(), segmentValue.offset(), new LineAttribute[0]);
                            segment.copyFrom(mapper.segment(lineInfo));
                            return 0;
                        }),
                gpioOperations -> {
                    var list = new ArrayList<LineInfo>();
                    gpioOperations.lines(new ChipInfo("chip name", "chip label", 3)).forEach(list::add);
                    assertThat(list).hasSize(3);
                    list.forEach(lineInfo -> assertThat(lineInfo.name()).isEqualTo("line " + lineInfo.offset()));
                }
        );
    }

    @Test
    void canReadLine() {
        performTest(
                builder -> defaultOperations("/dev/chip name").apply(builder)
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            assertThat(args[0]).isEqualTo(1);
                            assertThat(args[1]).isEqualTo(GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL);
                            var segment = (MemorySegment)args[2];
                            var segmentValue =  mapper.value(segment, LineInfo.class);
                            var lineInfo = new LineInfo("line " + segmentValue.offset(), "Test", segmentValue.offset(), segmentValue.offset(), new LineAttribute[0]);
                            segment.copyFrom(mapper.segment(lineInfo));
                            return 0;
                        }),
                gpioOperations -> {
                    var chip = new ChipInfo("chip name", "chip label", 2);
                    assertThat(gpioOperations.lineInfo(chip, 0))
                            .isEqualTo(new LineInfo("line 0", "Test", 0, 0, new LineAttribute[0]));
                    assertThat(gpioOperations.lineInfo(chip, 1))
                            .isEqualTo(new LineInfo("line 1", "Test", 1, 1, new LineAttribute[0]));
                }
        );
    }

    private UnaryOperator<FakeNativeContext.Builder> defaultOperations(String filename) {
        return builder -> builder
                .addMethodCaller("open", FileOperationsImpl.Descriptors.OPEN, args -> {
                    assertThat(((MemorySegment)args[0]).getString(0)).isEqualTo(filename);
                    return 1;
                })
                .addMethodCaller("fcntl", FileOperationsImpl.Descriptors.FCNTL, args -> 1)
                .addMethodCaller("close", FileOperationsImpl.Descriptors.CLOSE, args -> 0);
    }

    private void performTest(
            UnaryOperator<FakeNativeContext.Builder> configurer,
            Consumer<GpioOperations> verifier
    ) {
        var context = configurer.apply(FakeNativeContext.builder()).build();
        var fileOperations = new GpioOperationsImpl(context);
        verifier.accept(fileOperations);

        var methodCallerFactory = (FakeMethodCallerFactory)context.getMethodCallerFactory();
        methodCallerFactory.assertInvoked();
    }
}