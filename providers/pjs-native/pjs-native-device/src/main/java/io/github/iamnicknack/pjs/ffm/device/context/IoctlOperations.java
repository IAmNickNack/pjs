package io.github.iamnicknack.pjs.ffm.device.context;

public interface IoctlOperations {
    /**
     * Perform an ioctl operation with an integer argument passed by reference.
     * @param fd the spi device file descriptor
     * @param command the ioctl command
     * @param data the integer data to pass
     * @return data modified by the ioctl call
     */
    int ioctl(int fd, long command, int data);

    default int ioctl(FileDescriptor fd, long command, int data) {
        return ioctl(fd.fd(), command, data);
    }

    /**
     * Perform an ioctl read/write-like operation with arbitrary data passed by reference.
     * @param fd the spi device file descriptor
     * @param command the ioctl command
     * @param data the data to pass
     * @param type the class of the data
     * @return data modified by the ioctl call
     * @param <T> the type of data
     */
    <T> T ioctl(int fd, long command, T data, Class<T> type);

    default <T> T ioctl(FileDescriptor fd, long command, T data, Class<T> type) {
        return ioctl(fd.fd(), command, data, type);
    }

    /**
     * Perform an ioctl read/write-like operation with arbitrary data passed by reference.
     * @param fd the spi device file descriptor
     * @param command the ioctl command
     * @param data the data to pass
     * @return data modified by the ioctl call
     * @param <T> the type of data
     */
    <T> T ioctl(int fd, long command, T data);

    default <T> T ioctl(FileDescriptor fd, long command, T data) {
        return ioctl(fd.fd(), command, data);
    }

    /**
     * Perform an ioctl read-like operation.
     * @param fd the spi device file descriptor
     * @param command the ioctl command
     * @param type the type of data to be read
     * @return data modified by the ioctl call
     * @param <T> the type of data
     */
    <T> T ioctl(int fd, long command, Class<T> type);

    default <T> T ioctl(FileDescriptor fd, long command, Class<T> type) {
        return ioctl(fd.fd(), command, type);
    }
}
