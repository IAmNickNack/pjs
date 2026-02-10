package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public class IoctlOperationsImpl implements IoctlOperations {

    private  final NativeContext nativeContext;

    private final NativeContext.MethodCaller ioctlIntByReference;

    public IoctlOperationsImpl(NativeContext nativeContext) {
        this.nativeContext = nativeContext;
        this.ioctlIntByReference = nativeContext.capturedStateMethodCaller("ioctl", Descriptors.IOCTL_INT_BY_REFERENCE);
    }

    public IoctlOperationsImpl(SegmentAllocator segmentAllocator) {
        this(new NativeContext(segmentAllocator));
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
        var dataMemorySegment = nativeContext.segment(data, type);
        ioctlIntByReference.call(fd, command, dataMemorySegment);
        return nativeContext.convertValue(dataMemorySegment, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T ioctl(int fd, long command, T data) {
        return ioctl(fd, command, data, (Class<T>) data.getClass());
    }

    @Override
    public <T> T ioctl(int fd, long command, Class<T> type) {
        var data = nativeContext.allocate(nativeContext.layout(type));
        ioctlIntByReference.call(fd, command, data);
        return nativeContext.convertValue(data, type);
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
