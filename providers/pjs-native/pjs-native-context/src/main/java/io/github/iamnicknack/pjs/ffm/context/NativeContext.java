package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.foreign.SegmentAllocator;

/**
 * Container for components required to interact with native code.
 */
public interface NativeContext {
    /**
     * Checks if the current system is a Raspberry Pi.
     * This is determined by checking the /proc/cpuinfo file for the presence of the string "raspberry pi".
     *
     * @return true if the system is a Raspberry Pi, false otherwise
     */
    static boolean isAvailable() {
        var cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            return false;
        }

        try (var reader = new BufferedReader(new FileReader(cpuInfo))) {
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

    /**
     * Common {@link SegmentAllocator}
     */
    SegmentAllocator getSegmentAllocator();

    /**
     * Common {@link MemorySegmentMapper}
     */
    MemorySegmentMapper getMemorySegmentMapper();

    /**
     * Common {@link MethodCallerFactory}
     */
    MethodCallerFactory getMethodCallerFactory();

    /**
     * Common {@link MethodCallerFactory} for capturing error state.
     */
    MethodCallerFactory getCapturedStateMethodCallerFactory();
}
