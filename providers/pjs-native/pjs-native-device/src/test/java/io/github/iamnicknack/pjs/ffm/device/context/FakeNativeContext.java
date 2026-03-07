package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerCustomizer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.jspecify.annotations.NonNull;

import java.lang.foreign.Arena;
import java.lang.foreign.SegmentAllocator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FakeNativeContext implements NativeContext {

    private final SegmentAllocator segmentAllocator = Arena.ofAuto();
    private final MemorySegmentMapper memorySegmentMapper = new MemorySegmentMapperImpl(segmentAllocator);
    private final FakeMethodCallerCustomizer fakeMethodCallerCustomizer;

    FakeNativeContext(Map<String, MethodCaller> methodCallerLookup) {
        this.fakeMethodCallerCustomizer = new FakeMethodCallerCustomizer(methodCallerLookup);
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
    public MethodCallerCustomizer getMethodCallerCustomizer() {
        return fakeMethodCallerCustomizer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, MethodCaller> methodCallerLookup = new HashMap<>();

        public Builder addMethodCaller(String name, MethodCaller caller) {
            methodCallerLookup.put(name, caller);
            return this;
        }

        public FakeNativeContext build() {
            return new FakeNativeContext(Collections.unmodifiableMap(methodCallerLookup));
        }
    }
}
