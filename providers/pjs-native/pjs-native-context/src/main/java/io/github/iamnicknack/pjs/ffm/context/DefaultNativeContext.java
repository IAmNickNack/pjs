package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.method.DefaultMethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;

import java.lang.foreign.Arena;
import java.lang.foreign.SegmentAllocator;

/**
 * Container for components required to interact with native code.
 */
public class DefaultNativeContext implements NativeContext {

    private final SegmentAllocator segmentAllocator;

    private final MemorySegmentMapper memorySegmentMapper;

    private final MethodCallerFactory methodCallerFactory;

    public DefaultNativeContext() {
        this(Arena.ofAuto());
    }

    public DefaultNativeContext(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, new MemorySegmentMapperImpl(segmentAllocator));
    }

    public DefaultNativeContext(SegmentAllocator segmentAllocator, MemorySegmentMapper memorySegmentMapper) {
        this.segmentAllocator = segmentAllocator;
        this.memorySegmentMapper = memorySegmentMapper;
        this.methodCallerFactory = new DefaultMethodCallerFactory(segmentAllocator);
    }

    @Override
    public SegmentAllocator getSegmentAllocator() {
        return segmentAllocator;
    }

    @Override
    public MemorySegmentMapper getMemorySegmentMapper() {
        return memorySegmentMapper;
    }

    @Override
    public MethodCallerFactory getMethodCallerFactory() {
        return methodCallerFactory;
    }
}
