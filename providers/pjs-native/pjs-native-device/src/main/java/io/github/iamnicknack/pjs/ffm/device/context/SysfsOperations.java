package io.github.iamnicknack.pjs.ffm.device.context;

import java.nio.file.Path;

/**
 * Operations which can be performed using sysfs
 */
public interface SysfsOperations {
    /**
     * The path to the device
     */
    Path path();

    void write(String path, byte[] value);

    byte[] read(String path);

    boolean exists();

    boolean exists(String path);

    void writeString(String path, String value);

    String readString(String path);

    void writeLong(String path, long value);

    long readLong(String path);

    void writeInt(String path, int value);

    long readInt(String path);
}
