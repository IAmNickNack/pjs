package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public class IoctlOperationsImpl implements IoctlOperations {

    private final SegmentAllocator nativeContext;
    private final MethodCaller ioctlIntByReference;
    private final MemorySegmentMapper memorySegmentMapper;

    public IoctlOperationsImpl(NativeContext nativeContext) {
        this.nativeContext = nativeContext.getSegmentAllocator();
        this.ioctlIntByReference = nativeContext.getCapturedStateMethodCallerFactory()
                .create("ioctl", Descriptors.IOCTL_INT_BY_REFERENCE);
        this.memorySegmentMapper = nativeContext.getMemorySegmentMapper();
    }

    public IoctlOperationsImpl(SegmentAllocator segmentAllocator) {
        this(new DefaultNativeContext(segmentAllocator));
    }

    @Override
    public int ioctl(int fd, long command, int data) {
        var dataMemorySegment = nativeContext.allocate(ValueLayout.JAVA_INT);
        dataMemorySegment.set(ValueLayout.JAVA_INT, 0, data);
        ioctlIntByReference.call(fd, command, dataMemorySegment);
        return dataMemorySegment.get(ValueLayout.JAVA_INT, 0);
    }

    @Override
    public <T> T ioctl(int fd, long command, T data, Class<T> type) {
        var dataMemorySegment = memorySegmentMapper.segment(data, type);
        ioctlIntByReference.call(fd, command, dataMemorySegment);
        return memorySegmentMapper.value(dataMemorySegment, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T ioctl(int fd, long command, T data) {
        return ioctl(fd, command, data, (Class<T>) data.getClass());
    }

    @Override
    public <T> T ioctl(int fd, long command, Class<T> type) {
        var data = nativeContext.allocate(memorySegmentMapper.layout(type));
        ioctlIntByReference.call(fd, command, data);
        return memorySegmentMapper.value(data, type);
    }

    private static class Descriptors {
        static final FunctionDescriptor IOCTL_INT_BY_VALUE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return type
                ValueLayout.JAVA_INT,   // int fd
                ValueLayout.JAVA_LONG,  // unsigned long request
                ValueLayout.JAVA_LONG   // unsigned long arg
        );

        static final FunctionDescriptor IOCTL_INT_BY_REFERENCE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return type
                ValueLayout.JAVA_INT,   // int fd
                ValueLayout.JAVA_LONG,  // unsigned long request
                ValueLayout.ADDRESS     // void *argp
        );

        static final FunctionDescriptor IOCTL_LONG_BY_REFERENCE = FunctionDescriptor.of(
                ValueLayout.JAVA_LONG,  // return type
                ValueLayout.JAVA_INT,   // int fd
                ValueLayout.JAVA_LONG,  // unsigned long request
                ValueLayout.ADDRESS     // void *argp
        );
    }

}
