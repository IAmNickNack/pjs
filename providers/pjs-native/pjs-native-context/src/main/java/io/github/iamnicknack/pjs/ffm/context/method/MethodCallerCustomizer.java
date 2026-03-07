package io.github.iamnicknack.pjs.ffm.context.method;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;

/**
 * Factory for creating named caller instances which wrap another {@link MethodCaller}.
 * <p>
 * This can be used during the creation of a {@link NativeContext} to proxy the default instance.
 * During testing this also allows a native method to be swapped out for a fake implementation.
 */
@FunctionalInterface
public interface MethodCallerCustomizer {
    /**
     * Wraps the given MethodCaller with a new MethodCaller instance.
     * @param name the name of the new MethodCaller
     * @param methodCaller the MethodCaller to wrap
     * @return a new MethodCaller instance
     */
    MethodCaller customize(String name, MethodCaller methodCaller);

    /**
     * A factory that returns the given MethodCaller unchanged.
     */
    MethodCallerCustomizer NOOP = (_, methodCaller) -> methodCaller;
}
