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
import java.util.concurrent.Callable;

public class CapturedStateWrapper {

    private static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
    private static final VarHandle ERRNO_HANDLE = CAPTURED_STATE_LAYOUT
            .varHandle(MemoryLayout.PathElement.groupElement("errno"));
    private static final MethodHandle STR_ERROR = Linker.nativeLinker()
            .downcallHandle(
                    Linker.nativeLinker().defaultLookup().find("strerror").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

    private final SegmentAllocator segmentAllocator;

    public CapturedStateWrapper(SegmentAllocator segmentAllocator) {
        this.segmentAllocator = segmentAllocator;
    }

    public int wrap(ResultSupplier supplier) {
        var capturedState = segmentAllocator.allocate(CAPTURED_STATE_LAYOUT);
        try {
            int result = supplier.get(capturedState);

            if (result < 0) {
                int errno = (int) ERRNO_HANDLE.get(capturedState, 0L);
                var errnoStr = (MemorySegment) STR_ERROR.invokeExact(errno);
                throw new CapturedStateException(errno, errnoStr.getString(0L));
            }
            
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ResultSupplier {
        int get(MemorySegment capturedState) throws Throwable;
    }

    public static class CapturedStateException extends RuntimeException {

        private final int errno;

        public CapturedStateException(int errno, String message) {
            super(message);
            this.errno = errno;
        }

        public int getErrno() {
            return errno;
        }
    }
}
