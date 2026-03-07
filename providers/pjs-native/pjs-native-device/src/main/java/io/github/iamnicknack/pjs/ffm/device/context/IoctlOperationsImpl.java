package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.method.CapturedStateWrapper;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

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
        this(
                nativeContext.getSegmentAllocator(),
                nativeContext.getMethodCallerCustomizer().customize(
                        IntByReferenceMethodCaller.METHOD_NAME,
                        new IntByReferenceMethodCaller(new CapturedStateWrapper(nativeContext.getSegmentAllocator()))
                ),
                nativeContext.getMemorySegmentMapper()
        );
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

    /**
     * Method caller for ioctl(int fd, long command, void * pointer)
     * <p>
     * A specific implementation avoids use of {@link MethodHandle#invokeWithArguments(Object...)} in favor of
     * {@link MethodHandle#invokeExact(Object...)}.
     */
    public static class IntByReferenceMethodCaller implements MethodCaller {

        public static final String METHOD_NAME = "ioctl";

        private final CapturedStateWrapper capturedStateWrapper;
        private final MethodHandle methodHandle;

        public IntByReferenceMethodCaller() {
            this(new CapturedStateWrapper(Arena.ofAuto()));
        }

        public IntByReferenceMethodCaller(CapturedStateWrapper capturedStateWrapper) {
            this.capturedStateWrapper = capturedStateWrapper;
            this.methodHandle = Linker.nativeLinker()
                    .downcallHandle(
                            Linker.nativeLinker().defaultLookup().find(METHOD_NAME).orElseThrow(),
                            IOCTL_INT_BY_REFERENCE,
                            Linker.Option.captureCallState("errno")
                    );
        }

        @Override
        public Object call(Object... args) {
            return capturedStateWrapper.wrap(capturedState ->
                    (int)methodHandle.invokeExact(
                            capturedState,
                            (int)args[0],
                            (long)args[1],
                            (MemorySegment)args[2]
                    )
            );
        }
    }

}
