package io.github.iamnicknack.pjs.ffm.device.context;

import java.lang.foreign.MemorySegment;
import java.util.function.BiFunction;

public interface FileOperations {

    /**
     * Open a file and return its file descriptor.
     * @param pathname the path to the file
     * @param flags the flags for opening the file
     * @return the file descriptor, or -1 on error
     */
    int open(String pathname, int flags);

    /**
     * Open a file and return a managed file descriptor.
     * @param pathname the path to the file
     * @param flags the flags for opening the file
     * @return the file descriptor
     */
    FileDescriptor openFd(String pathname, int flags);

    /**
     * Close a file descriptor.
     * @param fd the file descriptor to close
     * @return 0 on success, -1 on error
     */
    int close(int fd);

    default int close(FileDescriptor fd) {
        return close(fd.fd());
    }

    /**
     * Read from a file descriptor into a byte array.
     * @param fd the file descriptor to read from
     * @param buffer the buffer to read into
     * @param offset the offset to start reading from
     * @param count the number of bytes to read
     * @return the number of bytes read, or -1 on error
     */
    int read(int fd, byte[] buffer, int offset, int count);

    default int read(FileDescriptor fd, byte[] buffer, int offset, int count) {
        return read(fd.fd(), buffer, offset, count);
    }

    /**
     * Read from a file descriptor into a MemorySegment, processed by a handler.
     * @param fd the file descriptor to read from
     * @param offset the offset to start reading from
     * @param length the number of bytes to read
     * @param handler a function to interpret the MemorySegment
     * @return the result of the handler
     * @param <T> the return type of the handler
     */
    <T> T read(int fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler);

    default <T> T read(FileDescriptor fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler) {
        return read(fd.fd(), offset, length, handler);
    }

    /**
     * Write to a file descriptor from a byte array.
     * @param fd the file descriptor to write to
     * @param buffer the buffer to write from
     * @param offset the offset to start writing from
     * @param count the number of bytes to write
     * @return the number of bytes written, or -1 on error
     */
    int write(int fd, byte[] buffer, int offset, int count);

    default int write(FileDescriptor fd, byte[] buffer, int offset, int count) {
        return write(fd.fd(), buffer, offset, count);
    }

    int access(String pathname, int mode);

    /**
     * Check if the file exists at the given path
     * @param pathname the path to the file
     * @return true if the file exists
     */
    boolean exists(String pathname);

    /**
     * Check that the file descriptor represents a valid, open file
     * TODO: Make this function make sense. `is` shouldn't return `int`
     * @param fd the native file descriptor
     * @return an indicator
     */
    int isValid(int fd);

    /**
     * Create a new file descriptor instance which delegates operations to this instance
     * @param fd the native file descriptor
     * @return a wrapper around the native file descriptor
     */
    FileDescriptor createFileDescriptor(int fd);

    /**
     * Native file operation flags
     */
    class Flags {
        public static final int O_RDONLY = 0;           // Open for reading only
        public static final int O_WRONLY = 1;           // Open for writing only
        public static final int O_RDWR = 2;             // Open for reading and writing
        public static final int O_CREAT = 0x40;         // Create file if it does not exist (linux)
//        public static final int O_CREAT = 0x200;         // Create file if it does not exist (mac)
        public static final int O_EXCL = 0x80;          // Error if O_CREAT and the file exists
        public static final int O_TRUNC = 0x200;        // Truncate file to zero length
        public static final int O_APPEND = 0x400;       // Append on each write
        public static final int O_CLOEXEC = 0x80000;    // Set close-on-exec flag

        public static final int F_OK = 0;               // Test for existence of file
        public static final int R_OK = 0x04;            // Test for read permission
    }

    /**
     * Default base implementation which allows specific operations to be overridden.
     * This is primarily useful for tests.
     */
    abstract class AbstractFileOperations implements FileOperations {
        @Override
        public int open(String pathname, int flags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileDescriptor openFd(String pathname, int flags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int close(int fd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(int fd, byte[] buffer, int offset, int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T read(int fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int write(int fd, byte[] buffer, int offset, int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int access(String pathname, int mode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean exists(String pathname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int isValid(int fd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileDescriptor createFileDescriptor(int fd) {
            throw new UnsupportedOperationException();
        }
    }
}
