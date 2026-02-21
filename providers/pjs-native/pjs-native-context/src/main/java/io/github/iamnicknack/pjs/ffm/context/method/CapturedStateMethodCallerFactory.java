package io.github.iamnicknack.pjs.ffm.context.method;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

/**
 * Creates a MethodCaller for the specified native method name and function descriptor.
 * This MethodCaller is designed to work with functions that utilize a CapturedState structure.
 * It automatically allocates a CapturedState instance and prepends it to the argument list
 * when invoking the native method.
 */
public class CapturedStateMethodCallerFactory implements MethodCallerFactory {

    // Captured state for errno
    private static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
    // Errno var handle
    private static final VarHandle ERRNO_HANDLE = CAPTURED_STATE_LAYOUT
            .varHandle(MemoryLayout.PathElement.groupElement("errno"));
    // Strerror method handle (returns char* as an ADDRESS)
    private static final MethodHandle STR_ERROR = Linker.nativeLinker()
            .downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("strerror").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

    private final SegmentAllocator segmentAllocator;
    private final SymbolLookup symbolLookup;

    public CapturedStateMethodCallerFactory(SegmentAllocator segmentAllocator, SymbolLookup symbolLookup) {
        this.segmentAllocator = segmentAllocator;
        this.symbolLookup = symbolLookup;
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
    @Override
    public MethodCaller create(String name, FunctionDescriptor functionDescriptor) {
        var methodHandle = Linker.nativeLinker().downcallHandle(
                symbolLookup.find(name).orElseThrow(),
                functionDescriptor,
                Linker.Option.captureCallState("errno")
        );
        return args -> {
            var capturedState = segmentAllocator.allocate(CAPTURED_STATE_LAYOUT);
            var argsWithState = new Object[args.length + 1];
            argsWithState[0] = capturedState;
            System.arraycopy(args, 0, argsWithState, 1, args.length);

            try {
                var result = (int) methodHandle.invokeWithArguments(argsWithState);

                if (result < 0) {
                    int errno = (int) ERRNO_HANDLE.get(capturedState, 0L);
                    var errnoStr = (MemorySegment) STR_ERROR.invokeExact(errno);
                    throw new CapturedStateException(name, errno, errnoStr.getString(0L));
                }
                return result;
            } catch (Throwable e) {
                throw new MethodCaller.MethodCallerException(name, e);
            }
        };
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
