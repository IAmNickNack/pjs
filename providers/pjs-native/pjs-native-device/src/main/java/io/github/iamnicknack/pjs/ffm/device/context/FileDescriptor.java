package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.model.SerialReadOperation;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;

/**
 * Wrapper around a native file descriptor which can be closed
 * without access to a {@link FileOperationsImpl} instance
 */
public class FileDescriptor implements AutoCloseable, SerialWriteOperation, SerialReadOperation {
    private final FileOperations fileOperations;
    private final int fd;

    FileDescriptor(FileOperations fileOperations, int fd) {
        this.fileOperations = fileOperations;
        this.fd = fd;
    }

    public int fd() {
        return fd;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        fileOperations.write(fd, buffer, offset, length);
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        return fileOperations.read(fd, buffer, offset, length);
    }

    @Override
    public void close() {
        if (fileOperations.isValid(fd) >= 0) {
            fileOperations.close(fd);
        }
    }
}
