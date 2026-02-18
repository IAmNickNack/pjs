package io.github.iamnicknack.pjs.ffm.device.context;

import java.nio.file.Files;
import java.nio.file.Path;

public class SysfsOperationsImpl implements SysfsOperations {

    private static final int MAX_FILE_SIZE = 10;

    private final FileOperations fileOperations;
    private final Path devicePath;

    public SysfsOperationsImpl(Path devicePath, FileOperations fileOperations) {
        this.devicePath = devicePath;
        this.fileOperations = fileOperations;
    }

    @Override
    public Path path() {
        return devicePath;
    }

    @Override
    public void write(String path, byte[] value) {
        try (var fd = fileOperations.openFd(devicePath.resolve(path).toString(), FileOperationsImpl.Flags.O_WRONLY)) {
            fileOperations.write(fd, value, 0, Math.min(value.length, MAX_FILE_SIZE));
        }
    }

    @Override
    public byte[] read(String path) {
        try (var fd = fileOperations.openFd(devicePath.resolve(path).toString(), FileOperationsImpl.Flags.O_RDONLY)) {
            var buffer = new byte[MAX_FILE_SIZE];
            var bytesRead = fileOperations.read(fd, buffer, 0, MAX_FILE_SIZE);
            var result = new byte[bytesRead];
            System.arraycopy(buffer, 0, result, 0, bytesRead);
            return result;
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(devicePath);
    }

    @Override
    public boolean exists(String path) {
        return devicePath.resolve(path).toFile().exists();
    }
}
