package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public interface MethodCallerFactory {
    MethodCaller createNonCapture(String name, FunctionDescriptor descriptor, Invocation invocation);
    MethodCaller createCapturedState(String name, FunctionDescriptor descriptor, InvocationWithCapturedState invocation);

    interface Invocation {
        Object invoke(MethodHandle methodHandle, Object... args) throws Throwable;
    }

    interface InvocationWithCapturedState {
        int invoke(MethodHandle methodHandle, MemorySegment capturedState, Object... args) throws Throwable;
    }
}
