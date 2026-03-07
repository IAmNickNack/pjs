package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;

/**
 * Container for components required to interact with native code.
 */
public class DefaultNativeContext implements NativeContext {

    private final SegmentAllocator segmentAllocator;

    private final MemorySegmentMapper memorySegmentMapper;

    public DefaultNativeContext() {
        this(Arena.ofAuto());
    }

    public DefaultNativeContext(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, new MemorySegmentMapperImpl(segmentAllocator));
    }

    public DefaultNativeContext(SegmentAllocator segmentAllocator, MemorySegmentMapper memorySegmentMapper) {
        this.segmentAllocator = segmentAllocator;
        this.memorySegmentMapper = memorySegmentMapper;
    }

    @Override
    public SegmentAllocator getSegmentAllocator() {
        return segmentAllocator;
    }

    @Override
    public MemorySegmentMapper getMemorySegmentMapper() {
        return memorySegmentMapper;
    }
}
