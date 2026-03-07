package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.CapturedStateWrapper;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SegmentAllocator;

public class MethodCallerFactoryImpl implements MethodCallerFactory {

    private final CapturedStateWrapper capturedStateWrapper;

    public MethodCallerFactoryImpl(SegmentAllocator segmentAllocator) {
        this.capturedStateWrapper = new CapturedStateWrapper(segmentAllocator);
    }

    @Override
    public MethodCaller createNonCapture(String name, FunctionDescriptor descriptor, Invocation invocation) {
        var methodHandle = Linker.nativeLinker()
                .downcallHandle(
                        Linker.nativeLinker()
                                .defaultLookup()
                                .find(name)
                                .orElseThrow(),
                        descriptor
                );

        return args -> {
            try {
                return invocation.invoke(methodHandle, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public MethodCaller createCapturedState(String name, FunctionDescriptor descriptor, InvocationWithCapturedState invocation) {
        var methodHandle = Linker.nativeLinker()
                .downcallHandle(
                        Linker.nativeLinker()
                                .defaultLookup()
                                .find(name)
                                .orElseThrow(),
                        descriptor,
                        Linker.Option.captureCallState("errno")
                );

        return args -> capturedStateWrapper.wrap(capturedState -> {
            try {
                return invocation.invoke(methodHandle, capturedState, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
