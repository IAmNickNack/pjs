package io.github.iamnicknack.pjs.ffm.context.method;

import java.lang.foreign.FunctionDescriptor;

/**
 * A factory for creating MethodCaller instances.
 */
@FunctionalInterface
public interface MethodCallerFactory {
    /**
     * Creates a MethodCaller for the specified native method name and function descriptor.
     * @param name the name of the native method
     * @param functionDescriptor the function descriptor of the native method
     * @return a MethodCaller instance for invoking the native method
     */
    MethodCaller create(String name, FunctionDescriptor functionDescriptor);
}
