package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.util.function.BiFunction;

public class FileOperationsImpl implements FileOperations {

    private final SegmentAllocator segmentAllocator;
    private final MethodCaller openCreate;
    private final MethodCaller open;
    private final MethodCaller close;
    private final MethodCaller read;
    private final MethodCaller write;
    private final MethodCaller access;
    private final MethodCaller fcntl;

    public FileOperationsImpl(NativeContext nativeContext) {
        this.segmentAllocator = nativeContext.getSegmentAllocator();

        var methodCallerFactory = nativeContext.getMethodCallerFactory();
        var capturedStateMethodCallerFactory = nativeContext.getCapturedStateMethodCallerFactory();

        this.openCreate = capturedStateMethodCallerFactory.create("open", Descriptors.OPEN_CREATE);
        this.open = capturedStateMethodCallerFactory.create("open", Descriptors.OPEN);
        this.close = capturedStateMethodCallerFactory.create("close", Descriptors.CLOSE);
        this.read = capturedStateMethodCallerFactory.create("read", Descriptors.READ);
        this.write = capturedStateMethodCallerFactory.create("write", Descriptors.WRITE);
        this.access = methodCallerFactory.create("access", Descriptors.ACCESS);
        this.fcntl = methodCallerFactory.create("fcntl", Descriptors.FCNTL);
    }

    @Override
    public int open(String pathname, int flags) {
        var path = segmentAllocator.allocateFrom(pathname);
        if ((flags & Flags.O_CREAT) != 0) {
            var mode = 0644;
            return (int) openCreate.call(path, flags, mode); // Default portMode if not specified
        } else {
            return (int) open.call(path, flags);
        }
    }

    @Override
    public FileDescriptor openFd(String pathname, int flags) {
        return new FileDescriptor(this, open(pathname, flags));
    }

    @Override
    public int close(int fd) {
        return (int) close.call(fd);
    }

    @Override
    public int read(int fd, byte[] buffer, int offset, int count) {
        return this.read(fd, offset, count, (segment, bytesRead) -> {
            if (bytesRead > 0) {
                segment.asByteBuffer().get(buffer, offset, bytesRead);
            }
            return bytesRead;
        });
    }

    @Override
    public <T> T read(int fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler) {
        var buf = segmentAllocator.allocate(ValueLayout.JAVA_BYTE, length);
        int bytesRead = (int) read.call(fd, buf, length);
        return handler.apply(buf, bytesRead);
    }

    @Override
    public int write(int fd, byte[] buffer, int offset, int count) {
        var segment = segmentAllocator.allocate(count);
        segment.asByteBuffer().put(buffer, offset, count);
        return (int) write.call(fd, segment, count);
    }

    @Override
    public int access(String pathname, int mode) {
        var path = segmentAllocator.allocateFrom(pathname);
        return (int) access.call(path, mode);
    }

    @Override
    public boolean exists(String pathname) {
        return access(pathname, Flags.F_OK) == 0;
    }

    @Override
    public int isValid(int fd) {
        return (int) fcntl.call(fd, 1); // F_GETFD is 0
    }

    @Override
    public FileDescriptor createFileDescriptor(int fd) {
        return new FileDescriptor(this, fd);
    }

    /**
     * Native file operation descriptors
     */
    static class Descriptors {
        static final FunctionDescriptor OPEN_CREATE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // return type
                ValueLayout.ADDRESS,  // const char *pathname
                ValueLayout.JAVA_INT, // int flags
                ValueLayout.JAVA_INT  // mode_t portMode (optional, used with O_CREAT)
        );

        static final FunctionDescriptor OPEN = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // return type
                ValueLayout.ADDRESS,  // const char *pathname
                ValueLayout.JAVA_INT  // int flags
        );

        static final FunctionDescriptor CLOSE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, // return type
                ValueLayout.JAVA_INT  // int fd
        );

        static final FunctionDescriptor READ = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return type (ssize_t)
                ValueLayout.JAVA_INT,  // int fd
                ValueLayout.ADDRESS,   // void *buf
                ValueLayout.JAVA_INT   // size_t count
        );

        static final FunctionDescriptor WRITE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,  // return type (ssize_t)
                ValueLayout.JAVA_INT,  // int fd
                ValueLayout.ADDRESS,   // const void *buf
                ValueLayout.JAVA_INT   // size_t count
        );

        static final FunctionDescriptor ACCESS = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT
        );

        static final FunctionDescriptor FCNTL = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT
        );
    }

}
