package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants;
import io.github.iamnicknack.pjs.logging.LoggingSpi;

import java.lang.foreign.SegmentAllocator;

public class NativeSpiProvider implements SpiProvider {

    private final FileOperations fileOperations;
    private final IoctlOperations ioctlOperations;
    private final MemorySegmentMapper memorySegmentMapper;
    private final SegmentAllocator segmentAllocator;

    public NativeSpiProvider(
            FileOperations fileOperations,
            IoctlOperations ioctlOperations,
            MemorySegmentMapper memorySegmentMapper,
            SegmentAllocator segmentAllocator
    ) {
        this.fileOperations = fileOperations;
        this.ioctlOperations = ioctlOperations;
        this.memorySegmentMapper = memorySegmentMapper;
        this.segmentAllocator = segmentAllocator;
    }

    @Override
    public Spi create(SpiConfig config) {
        var path = "/dev/spidev" + config.bus() + "." + config.chipSelect();
        var spiFileDescriptor = fileOperations.openFd(path, FileOperationsImpl.Flags.O_RDWR);

        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_MODE, config.mode());
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_MODE, config.mode());
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_BITS_PER_WORD, 8);
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_BITS_PER_WORD, 8);
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_MAX_SPEED_HZ, config.baudRate());
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_MAX_SPEED_HZ, config.baudRate());
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_LSB_FIRST, config.lsbFirst() ? 1 : 0);
        ioctlOperations.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_LSB_FIRST, config.lsbFirst() ? 1 : 0);

        return new NativeSpi(
                config,
                spiFileDescriptor,
                ioctlOperations,
                segmentAllocator,
                memorySegmentMapper
        );
    }

    @Override
    public SpiTransfer createTransfer(Spi spi) {
        var delegateSpi = (spi instanceof LoggingSpi loggingSpi) ? loggingSpi.getDelegate() : spi;
        return ((NativeSpi)delegateSpi).new Transfer();
    }
}
