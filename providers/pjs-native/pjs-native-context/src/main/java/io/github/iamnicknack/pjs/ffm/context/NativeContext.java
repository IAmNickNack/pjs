package io.github.iamnicknack.pjs.ffm.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;

import java.lang.foreign.SegmentAllocator;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

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
        return isAvailable(FileSystems.getDefault());
    }

    static boolean isAvailable(FileSystem fs) {
        var path = fs.getPath("/proc/cpuinfo");
        if (!Files.exists(path)) {
            return false;
        }

        try (var reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                var lower = line.toLowerCase();
                if (lower.startsWith("model") && lower.contains("raspberry pi")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
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
}
