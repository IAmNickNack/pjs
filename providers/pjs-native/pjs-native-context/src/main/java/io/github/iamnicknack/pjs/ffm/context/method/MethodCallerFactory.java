package io.github.iamnicknack.pjs.ffm.context.method;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

/**
 * Factory to allow delegation of native method call construction.
 * <p>
 * Delegating this responsibility has two main benefits:
 * <ul>
 *     <li>Encapsulates the complexity of native method call construction, reducing boilerplate code.
 *     <li>Allows for easier testing and mocking of native method calls. This is particularly useful for
 *         testing higher-level code on platforms that do not support specific native method calls.
 * </ul>
 */
public interface MethodCallerFactory {
    /**
     * Create a non-capturing method caller.
     * @param name the name of the method
     * @param descriptor the descriptor of the method
     * @param invocation the method invocation
     * @return a new MethodCaller instance
     */
    MethodCaller createNonCapture(String name, FunctionDescriptor descriptor, Invocation invocation);

    /**
     * Create a non-capturing method caller. This uses {@link MethodHandle#invokeWithArguments(Object...)} which
     * incurrs a performance overhead. Prefer {@link #createNonCapture(String, FunctionDescriptor, Invocation)}.
     * @param name the name of the method
     * @param descriptor the descriptor of the method
     * @return a new MethodCaller instance
     */
    default MethodCaller createNonCapture(String name, FunctionDescriptor descriptor) {
        return createNonCapture(name, descriptor, MethodHandle::invokeWithArguments);
    }

    /**
     * Create a capturing method caller, capable of capturing error state.
     * @param name the name of the method
     * @param descriptor the descriptor of the method
     * @param invocation the method invocation
     * @return a new MethodCaller instance
     */
    MethodCaller createCapturedState(String name, FunctionDescriptor descriptor, InvocationWithCapturedState invocation);

    /**
     * Create a capturing method caller, capable of capturing error state.
     * This uses {@link MethodHandle#invokeWithArguments(Object...)} which incurrs a performance overhead.
     * Prefer {@link #createCapturedState(String, FunctionDescriptor, InvocationWithCapturedState)}.
     * @param name the name of the method
     * @param descriptor the descriptor of the method
     * @return a new MethodCaller instance
     */
    default MethodCaller createCapturedState(String name, FunctionDescriptor descriptor) {
        return createCapturedState(name, descriptor, (methodHandle, capturedState, args) -> {
            var argsWithCapturedState = new Object[args.length + 1];
            argsWithCapturedState[0] = capturedState;
            System.arraycopy(args, 0, argsWithCapturedState, 1, args.length);
            return (int)methodHandle.invokeWithArguments(argsWithCapturedState);
        });
    }

    /**
     * Allows exact method invocations to be provided by the caller, rather than falling back on
     * {@link MethodHandle#invokeWithArguments(Object...)}
     */
    interface Invocation {
        /**
         * Invoke the method handle with the provided arguments.
         * @param methodHandle the method handle to invoke
         * @param args the arguments to pass to the method
         * @return the result of the method invocation
         * @throws Throwable if the method invocation throws an exception
         */
        Object invoke(MethodHandle methodHandle, Object... args) throws Throwable;
    }

    /**
     * Allows exact method invocations to be provided by the caller, rather than falling back on
     * {@link MethodHandle#invokeWithArguments(Object...)}
     */
    interface InvocationWithCapturedState {
        /**
         * Invoke the method handle with the provided arguments.
         * @param methodHandle the method handle to invoke
         * @param capturedState memory segment to hold captured state in case of an error
         * @param args the arguments to pass to the method
         * @return the result of the method invocation
         * @throws Throwable if the method invocation throws an exception
         */
        int invoke(MethodHandle methodHandle, MemorySegment capturedState, Object... args) throws Throwable;
    }
}
