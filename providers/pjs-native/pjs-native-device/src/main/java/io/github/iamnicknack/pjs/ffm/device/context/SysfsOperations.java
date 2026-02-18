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

    default void writeString(String path, String value) {
        write(path, value.getBytes());
    }

    default String readString(String path) {
        return new String(read(path));
    }

    default void writeLong(String path, long value) {
        writeString(path, Long.toString(value));
    }

    default long readLong(String path) {
        return Long.parseLong(readString(path).trim());
    }

    default void writeInt(String path, int value) {
        writeString(path, Integer.toString(value));
    }

    default long readInt(String path) {
        return Integer.parseInt(readString(path).trim());
    }
}
