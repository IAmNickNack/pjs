package io.github.iamnicknack.pjs.ffm.context.method;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;

/**
 * Creates a MethodCaller for the specified native method name and function descriptor.
 */
public class DefaultMethodCallerFactory implements MethodCallerFactory {

    private final SymbolLookup symbolLookup;

    public DefaultMethodCallerFactory(SymbolLookup symbolLookup) {
        this.symbolLookup = symbolLookup;
    }

    /**
     * Creates a MethodCaller for the specified native method name and function descriptor.
     * @param name the name of the native method
     * @param functionDescriptor the function descriptor of the native method
     * @return a MethodCaller instance for invoking the native method
     */
    @Override
    public MethodCaller create(String name, FunctionDescriptor functionDescriptor) {
        var methodHandle = Linker.nativeLinker()
                .downcallHandle(symbolLookup.find(name).orElseThrow(), functionDescriptor);
        return args -> {
            try {
                return methodHandle.invokeWithArguments(args);
            } catch (Throwable e) {
                throw new MethodCaller.MethodCallerException(name, e);
            }
        };
    }
}
