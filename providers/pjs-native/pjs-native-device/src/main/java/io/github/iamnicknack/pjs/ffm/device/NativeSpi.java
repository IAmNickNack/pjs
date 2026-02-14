package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants;
import io.github.iamnicknack.pjs.ffm.device.context.spi.SpiTransfer;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public class NativeSpi implements Spi, AutoCloseable {

    private final SpiConfig config;
    private final SegmentAllocator nativeContext;
    private final IoctlOperations ioctlOperations;
    private final FileDescriptor fileDescriptor;
    private final MemorySegmentMapper memorySegmentMapper;
    private final SpiTransfer.Serializer transferSerializer;

    public NativeSpi(SpiConfig config, NativeContext context, FileDescriptor fileDescriptor) {
        this.config = config;
        this.nativeContext = context.getSegmentAllocator();
        this.ioctlOperations = new IoctlOperationsImpl(context);
        this.fileDescriptor = fileDescriptor;
        this.memorySegmentMapper = context.getMemorySegmentMapper();
        this.transferSerializer = new SpiTransfer.Serializer(nativeContext);
    }

    @Override
    public SpiConfig getConfig() {
        return config;
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
        var writeBufferSegment = nativeContext.allocateFrom(ValueLayout.JAVA_BYTE, write);
        var outSegment = writeBufferSegment.asSlice(writeOffset, length);
        var inSegment = nativeContext.allocate(length);

        var transfer = SpiTransfer.builder()
                .txBuf(outSegment)
                .rxBuf(inSegment)
                .length(length)
                .build();

        var segment = memorySegmentMapper.segment(transfer, SpiTransfer.class);
        ioctlOperations.ioctl(fileDescriptor, SpiConstants.SPI_IOC_MESSAGE(1), segment, MemorySegment.class);

        byte[] bytesIn = inSegment.toArray(ValueLayout.JAVA_BYTE);
        System.arraycopy(bytesIn, 0, read, readOffset, length);

        return length;
    }

    @Override
    public void close() {
        fileDescriptor.close();
    }

    class Transfer implements io.github.iamnicknack.pjs.device.spi.SpiTransfer {

        @Override
        public int transfer(Message... messages) {
            var spiTransfers = new SpiTransfer[messages.length];
            for (int i = 0; i < messages.length; i++) {
                var currentTransfer = messages[i];
                var txBuffer = nativeContext.allocateFrom(ValueLayout.JAVA_BYTE, currentTransfer.sliceWrite());
                var rxBuffer = nativeContext.allocate(currentTransfer.length());
                spiTransfers[i] = SpiTransfer.builder()
                        .txBuf(txBuffer)
                        .rxBuf(rxBuffer)
                        .length(currentTransfer.length())
                        .delayUsecs(currentTransfer.delayUs())
                        .csChange(currentTransfer.csChange())
                        .build();
            }

            var segment = transferSerializer.serializeArray(spiTransfers, nativeContext);
            ioctlOperations.ioctl(
                    fileDescriptor,
                    SpiConstants.SPI_IOC_MESSAGE(spiTransfers.length),
                    segment,
                    MemorySegment.class
            );

            var byteCount = 0;
            for (int i = 0; i < messages.length; i++) {
                var buffer = spiTransfers[i];
                var currentTransfer = messages[i];
                System.arraycopy(
                        buffer.rxArray(),
                        0,
                        currentTransfer.read(),
                        currentTransfer.readOffset(),
                        currentTransfer.length()
                );
                byteCount += currentTransfer.length();
            }
            return byteCount;
        }
    }
}
