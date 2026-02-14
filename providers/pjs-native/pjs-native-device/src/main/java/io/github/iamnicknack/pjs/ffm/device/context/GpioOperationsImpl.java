package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations.Flags;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.ChipInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineInfo;

import java.util.stream.IntStream;

public class GpioOperationsImpl implements GpioOperations {

    private final FileOperations fileOperations;
    private final IoctlOperations ioctlOperations;

    public GpioOperationsImpl(NativeContext nativeContext) {
        this.fileOperations = new FileOperationsImpl(nativeContext);
        this.ioctlOperations = new IoctlOperationsImpl(nativeContext);
    }

    public GpioOperationsImpl(FileOperations fileOperations, IoctlOperations ioctlOperations) {
        this.fileOperations = fileOperations;
        this.ioctlOperations = ioctlOperations;
    }

    @Override
    public ChipInfo chipInfo(String devicePath) {
        try(var fd = fileOperations.openFd(devicePath, Flags.O_RDONLY | Flags.O_CLOEXEC)) {
            return ioctlOperations.ioctl(fd, GpioConstants.GPIO_GET_CHIPINFO_IOCTL, ChipInfo.class);
        }
    }

    @Override
    public Iterable<LineInfo> lines(ChipInfo chipInfo) {
        try (var fd = fileOperations.openFd(chipInfo.getPath(), Flags.O_RDONLY | Flags.O_CLOEXEC)) {
            return IntStream.range(0, chipInfo.lines())
                    .mapToObj(line -> ioctlOperations.ioctl(
                            fd,
                            GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL,
                            LineInfo.ofOffset(line),
                            LineInfo.class
                    ))
                    .toList();
        }
    }

    @Override
    public LineInfo lineInfo(ChipInfo chipInfo, int offset) {
        try (var fd = fileOperations.openFd(chipInfo.getPath(), Flags.O_RDONLY | Flags.O_CLOEXEC)) {
            return ioctlOperations.ioctl(
                    fd,
                    GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL,
                    LineInfo.ofOffset(offset),
                    LineInfo.class
            );
        }
    }
}
