package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.jspecify.annotations.NonNull;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SegmentAllocator;

public class FakeNativeContext implements NativeContext {

    private final SegmentAllocator segmentAllocator = Arena.ofAuto();
    private final MemorySegmentMapper memorySegmentMapper = new MemorySegmentMapperImpl(segmentAllocator);
    private final MethodCallerFactory methodCallerFactory;

    FakeNativeContext(MethodCallerFactory methodCallerFactory) {
        this.methodCallerFactory = methodCallerFactory;
    }

    @Override
    @NonNull
    public SegmentAllocator getSegmentAllocator() {
        return segmentAllocator;
    }

    @Override
    @NonNull
    public MemorySegmentMapper getMemorySegmentMapper() {
        return memorySegmentMapper;
    }

    @Override
    @NonNull
    public MethodCallerFactory getMethodCallerFactory() {
        return methodCallerFactory;
    }

    @Override
    @NonNull
    public MethodCallerFactory getCapturedStateMethodCallerFactory() {
        return methodCallerFactory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FakeMethodCallerFactory.Builder methodCallerFactoryBuilder = FakeMethodCallerFactory.builder();

        public Builder addMethodCaller(String name, FunctionDescriptor descriptor, MethodCaller caller) {
            methodCallerFactoryBuilder.add(name, descriptor, caller);
            return this;
        }

        public FakeNativeContext build() {
            return new FakeNativeContext(methodCallerFactoryBuilder.build());
        }
    }
}
