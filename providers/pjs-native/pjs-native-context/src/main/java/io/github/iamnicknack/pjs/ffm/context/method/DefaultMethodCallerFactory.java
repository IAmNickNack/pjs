package io.github.iamnicknack.pjs.ffm.context.method;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;

/**
 * Default implementation of {@link MethodCallerFactory}
 */
public class DefaultMethodCallerFactory implements MethodCallerFactory {

    private final CapturedStateWrapper capturedStateWrapper;
    private final SymbolLookup symbolLookup;
    private final Linker.Option capturedStateOption;

    /**
     * Default constructor using auto arena, default linker lookup and default call state.
     */
    public DefaultMethodCallerFactory() {
        this(Arena.ofAuto());
    }

    /**
     * Constructor using the provided segment allocator, default linker-lookup and default call state.
     * @param segmentAllocator the segment allocator to use
     */
    public DefaultMethodCallerFactory(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, Linker.nativeLinker().defaultLookup(), Linker.Option.captureCallState("errno"));
    }

    /**
     * Constructor using the provided segment allocator, linker-lookup and call state option.
     * @param segmentAllocator the segment allocator to use
     * @param symbolLookup the linker lookup to use
     * @param capturedStateOption the call state option to use
     */
    public DefaultMethodCallerFactory(
            SegmentAllocator segmentAllocator,
            SymbolLookup symbolLookup,
            Linker.Option capturedStateOption
    ) {
        this.capturedStateWrapper = new CapturedStateWrapper(segmentAllocator);
        this.symbolLookup = symbolLookup;
        this.capturedStateOption = capturedStateOption;
    }

    @Override
    public MethodCaller createNonCapture(String name, FunctionDescriptor descriptor, Invocation invocation) {
        var methodHandle = Linker.nativeLinker()
                .downcallHandle(
                        symbolLookup
                                .find(name)
                                .orElseThrow(),
                        descriptor
                );

        return args -> {
            try {
                return invocation.invoke(methodHandle, args);
            } catch (Throwable e) {
                throw new MethodCaller.MethodCallerException(name, e);
            }
        };
    }

    @Override
    public MethodCaller createCapturedState(String name, FunctionDescriptor descriptor, InvocationWithCapturedState invocation) {
        var methodHandle = Linker.nativeLinker()
                .downcallHandle(
                        symbolLookup
                                .find(name)
                                .orElseThrow(),
                        descriptor,
                        capturedStateOption
                );

        return args -> capturedStateWrapper.wrap(capturedState -> {
            try {
                return invocation.invoke(methodHandle, capturedState, args);
            } catch (Throwable e) {
                throw new MethodCaller.MethodCallerException(name, e);
            }
        });
    }
}
