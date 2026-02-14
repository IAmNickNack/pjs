package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.method.CapturedStateMethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.method.DefaultMethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.foreign.Linker;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;

/**
 * Container for components required to interact with native code.
 */
public class NativeContext {

    private final SegmentAllocator segmentAllocator;

    private final MemorySegmentMapper memorySegmentMapper;

    private final MethodCallerFactory methodCallerFactory;

    private final MethodCallerFactory capturedStateMethodCallerFactory;

    public NativeContext(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, Linker.nativeLinker().defaultLookup(), new MemorySegmentMapperImpl(segmentAllocator));
    }

    public NativeContext(SegmentAllocator segmentAllocator, SymbolLookup symbolLookup, MemorySegmentMapper memorySegmentMapper) {
        this.segmentAllocator = segmentAllocator;
        this.memorySegmentMapper = memorySegmentMapper;
        this.methodCallerFactory = new DefaultMethodCallerFactory(symbolLookup);
        this.capturedStateMethodCallerFactory = new CapturedStateMethodCallerFactory(segmentAllocator, symbolLookup);
    }

    public SegmentAllocator getSegmentAllocator() {
        return segmentAllocator;
    }

    public MemorySegmentMapper getMemorySegmentMapper() {
        return memorySegmentMapper;
    }

    public MethodCallerFactory getMethodCallerFactory() {
        return methodCallerFactory;
    }

    public MethodCallerFactory getCapturedStateMethodCallerFactory() {
        return capturedStateMethodCallerFactory;
    }

    /**
     * Checks if the current system is a Raspberry Pi.
     * This is determined by checking the /proc/cpuinfo file for the presence of the string "raspberry pi".
     * @return true if the system is a Raspberry Pi, false otherwise
     */
    public static boolean isAvailable() {
        var cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            return false;
        }

        try(var reader = new BufferedReader(new FileReader(cpuInfo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var lower = line.toLowerCase();
                if (lower.startsWith("model") && lower.contains("raspberry pi")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // do nothing
        }

        return false;
    }
}
