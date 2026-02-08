package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public class NativeContext implements SegmentAllocator, MemorySegmentMapper {

    private final SegmentAllocator segmentAllocator;

    private final SymbolLookup symbolLookup;

    private final MemorySegmentMapper memorySegmentMapper;

    public NativeContext(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, Linker.nativeLinker().defaultLookup(), new MemorySegmentMapperImpl(segmentAllocator));
    }

    public NativeContext(SegmentAllocator segmentAllocator, SymbolLookup symbolLookup, MemorySegmentMapper memorySegmentMapper) {
        this.segmentAllocator = segmentAllocator;
        this.symbolLookup = symbolLookup;
        this.memorySegmentMapper = memorySegmentMapper;
    }

    @Override
    public MemorySegment allocate(long bytesSize, long alignment) {
        return segmentAllocator.allocate(bytesSize, alignment);
    }

    @Override
    public <T> T convertValue(MemorySegment segment, Class<T> targetType) {
        return memorySegmentMapper.convertValue(segment, targetType);
    }

    @Override
    public <T> MemorySegment segment(T value, Class<T> sourceType) {
        return memorySegmentMapper.segment(value, sourceType);
    }

    public static boolean isAvailable() {
        var cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            return false;
        }

        try(var reader = new BufferedReader(new FileReader(cpuInfo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var lower = line.toLowerCase();
                if (lower.startsWith("model") && lower.contains("raspberry pi")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // do nothing
        }

        return false;
    }

    @Override
    public MemoryLayout layout(Class<?> type) {
        return memorySegmentMapper.layout(type);
    }

    /**
     * Creates a MethodCaller for the specified native method name and function descriptor.
     * @param name the name of the native method
     * @param functionDescriptor the function descriptor of the native method
     * @return a MethodCaller instance for invoking the native method
     */
    public MethodCaller methodCaller(String name, FunctionDescriptor functionDescriptor) {
        return new MethodHandleCaller(name, functionDescriptor);
    }

    /**
     * Creates a MethodCaller for the specified native method name and function descriptor.
     * This MethodCaller is designed to work with functions that utilize a CapturedState structure.
     * It automatically allocates a CapturedState instance and prepends it to the argument list
     * when invoking the native method.
     * @param name the name of the native method
     * @param functionDescriptor the function descriptor of the native method
     * @return a MethodCaller instance for invoking the native method with CapturedState
     */
    public MethodCaller capturedStateMethodCaller(String name, FunctionDescriptor functionDescriptor) {
        return new CapturedStateMethodHandleCaller(name, functionDescriptor);
    }

    class MethodHandleCaller implements MethodCaller {
        private final MethodHandle methodHandle;
        private final String methodName;

        public MethodHandleCaller(String name, FunctionDescriptor functionDescriptor) {
            this.methodName = name;
            this.methodHandle = Linker.nativeLinker().downcallHandle(
                    symbolLookup.find(name).orElseThrow(),
                    functionDescriptor
            );
        }

        @Override
        public Object call(Object... args) {
            try {
                return methodHandle.invokeWithArguments(args);
            } catch (Throwable e) {
                throw new MethodCallerException(methodName, e);
            }
        }
    }

    /**
     * A MethodCaller implementation that handles captured state and throws {@link CapturedStateException}, allowing
     * native error state to be propagated.
     */
    class CapturedStateMethodHandleCaller implements MethodCaller {

        // Captured state for errno
        private static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
        // Errno var handle
        private static final VarHandle ERRNO_HANDLE =
                CAPTURED_STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("errno"));
        // Pointer to string error function result
        private static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(
                MemoryLayout.sequenceLayout(1024, ValueLayout.JAVA_BYTE));
        // Strerror method handle
        static final MethodHandle STR_ERROR = Linker.nativeLinker().downcallHandle(
                Linker.nativeLinker().defaultLookup().find("strerror").orElseThrow(),
                FunctionDescriptor.of(POINTER, ValueLayout.JAVA_INT));


        private final MethodHandle methodHandle;
        private final String methodName;

        public CapturedStateMethodHandleCaller(String name, FunctionDescriptor functionDescriptor) {
            this.methodName = name;
            this.methodHandle = Linker.nativeLinker().downcallHandle(
                    symbolLookup.find(name).orElseThrow(),
                    functionDescriptor,
                    Linker.Option.captureCallState("errno")
            );
        }

        @Override
        public Object call(Object... args) {
            var capturedState = allocate(CAPTURED_STATE_LAYOUT);
            var argsWithState = new Object[args.length + 1];
            argsWithState[0] = capturedState;
            System.arraycopy(args, 0, argsWithState, 1, args.length);

            try {
                var result = (int) methodHandle.invokeWithArguments(argsWithState);

                if (result < 0) {
                    int errno = (int) ERRNO_HANDLE.get(capturedState, 0L);
                    var errnoStr = (MemorySegment) STR_ERROR.invokeExact(errno);
                    throw new CapturedStateException(this.methodName, errno, errnoStr.getString(0L));
                }
                return result;
            } catch (Throwable e) {
                throw new MethodCallerException(methodName, e);
            }
        }
    }

    /**
     * A functional interface for calling native methods with variable arguments.
     */
    @FunctionalInterface
    public interface MethodCaller {
        Object call(Object... args);
    }

    /**
     * An exception class for handling errors during method calls.
     */
    public static class MethodCallerException extends RuntimeException {
        private final String methodName;

        public MethodCallerException(String methodName, Throwable cause) {
            super("Error calling method: " + methodName, cause);
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }
    }

    /**
     * An exception class for handling errors related to CapturedState operations.
     */
    public static class CapturedStateException extends RuntimeException {
        private final String methodName;
        private final int errno;
        private final String errorMessage;

        public CapturedStateException(String methodName, int errno, String errorMessage) {
            super("CaptureState Error calling function: " + methodName + ",  - errno: " + errno + ", message: " + errorMessage);
            this.methodName = methodName;
            this.errno = errno;
            this.errorMessage = errorMessage;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getErrno() {
            return errno;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
