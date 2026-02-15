package io.github.iamnicknack.pjs.ffm.device.context;

import java.nio.file.Path;

/**
 * Factory for creating SysfsOperations instances.
 */
public interface SysfsOperationsFactory {
    /**
     * Create a new SysfsOperations instance.
     * @param devicePath the path to the device
     * @return a new SysfsOperations instance.
     */
    SysfsOperations createSysfsOperations(String devicePath);

    /**
     * Create a new SysfsOperations instance.
     * @param devicePath the path to the device
     * @return a new SysfsOperations instance.
     */
    default SysfsOperations createSysfsOperations(Path devicePath) {
        return this.createSysfsOperations(devicePath.toAbsolutePath().toString());
    }
}
