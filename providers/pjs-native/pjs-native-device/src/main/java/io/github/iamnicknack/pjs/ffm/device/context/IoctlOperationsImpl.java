package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

import static io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE;

public class IoctlOperationsImpl implements IoctlOperations {

    private final SegmentAllocator segmentAllocator;
    private final MethodCaller ioctlIntByReference;
    private final MemorySegmentMapper memorySegmentMapper;

    public IoctlOperationsImpl(
            SegmentAllocator segmentAllocator,
            MethodCaller ioctlIntByReference,
            MemorySegmentMapper memorySegmentMapper
    ) {
        this.segmentAllocator = segmentAllocator;
        this.ioctlIntByReference = ioctlIntByReference;
        this.memorySegmentMapper = memorySegmentMapper;
    }

    public IoctlOperationsImpl(NativeContext nativeContext) {
        MethodCallerFactory.InvocationWithCapturedState invocation = (methodHandle, capturedState, args) ->
                (int)methodHandle.invokeExact(capturedState, (int)args[0], (long)args[1], (MemorySegment)args[2]);
        MethodCaller methodCaller = nativeContext.getMethodCallerFactory()
                .createCapturedState("ioctl", IOCTL_INT_BY_REFERENCE, invocation);

        this(nativeContext.getSegmentAllocator(), methodCaller, nativeContext.getMemorySegmentMapper());
    }

    @Override
    public int ioctl(int fd, long command, int data) {
        var dataMemorySegment = segmentAllocator.allocate(ValueLayout.JAVA_INT);
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
        var data = segmentAllocator.allocate(memorySegmentMapper.layout(type));
        ioctlIntByReference.call(fd, command, data);
        return memorySegmentMapper.value(data, type);
    }

    static class Descriptors {
        static final FunctionDescriptor IOCTL_INT_BY_REFERENCE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return type
                ValueLayout.JAVA_INT,   // int fd
                ValueLayout.JAVA_LONG,  // unsigned long request
                ValueLayout.ADDRESS     // void *argp
        );
    }
}
