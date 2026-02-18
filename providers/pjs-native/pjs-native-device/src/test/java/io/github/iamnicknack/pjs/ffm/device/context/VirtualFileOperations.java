package io.github.iamnicknack.pjs.ffm.device.context;

import com.google.common.jimfs.Configuration;

import java.lang.foreign.MemorySegment;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class VirtualFileOperations implements FileOperations {

    private final FileSystem fileSystem = com.google.common.jimfs.Jimfs.newFileSystem(Configuration.unix());

    private final Map<Integer, FileDescriptorWithPath> fileDescriptorMap = new HashMap<>();
    private final Map<String, FileDescriptorWithPath> pathNameMap = new HashMap<>();

    private final Path root = fileSystem.getPath("");

    public Path root() {
        return root;
    }

    @Override
    public int open(String pathname, int flags) {
        return openFd(pathname, flags).fd();
    }

    @Override
    public FileDescriptor openFd(String pathname, int flags) {
        if (pathNameMap.containsKey(pathname)) {
            throw new IllegalArgumentException("File descriptor already exists for " + pathname);
        }

        var path = fileSystem.getPath(pathname);
        var fd = new FileDescriptorWithPath(path, pathNameMap.size() + 1);
        pathNameMap.put(path.toAbsolutePath().toString(), fd);
        fileDescriptorMap.put(fd.fd(), fd);

        return fd;
    }

    @Override
    public int close(int fd) {
        if (fileDescriptorMap.containsKey(fd)) {
            pathNameMap.remove(fileDescriptorMap.remove(fd).path.toAbsolutePath().toString());
            return 0;
        }
        return -1;
    }

    @Override
    public int read(int fd, byte[] buffer, int offset, int count) {
        if (fileDescriptorMap.containsKey(fd)) {
            try (var stream = Files.newInputStream(fileDescriptorMap.get(fd).path)) {
                return stream.read(buffer, offset, count);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return -1;
    }

    @Override
    public <T> T read(int fd, int offset, int length, BiFunction<MemorySegment, Integer, T> handler) {
        if (fileDescriptorMap.containsKey(fd)) {
            var buffer = new byte[length];
            try (var stream = Files.newInputStream(fileDescriptorMap.get(fd).path)) {
                stream.read(buffer, offset, length);
                return handler.apply(MemorySegment.ofArray(buffer), length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("File descriptor not found");
    }

    @Override
    public int write(int fd, byte[] buffer, int offset, int count) {
        if (fileDescriptorMap.containsKey(fd)) {
            try (var stream = Files.newOutputStream(fileDescriptorMap.get(fd).path)) {
                stream.write(buffer, offset, count);
                return count;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return -1;
    }

    @Override
    public int access(String pathname, int mode) {
        return 0;
    }

    @Override
    public boolean exists(String pathname) {
        return pathNameMap.containsKey(pathname) && pathNameMap.get(pathname).path.toFile().exists();
    }

    @Override
    public int isValid(int fd) {
        return 0;
    }

    @Override
    public FileDescriptor createFileDescriptor(int fd) {
        return new FileDescriptor(this, fd);
    }

    class FileDescriptorWithPath extends FileDescriptor {
        private final Path path;

        public FileDescriptorWithPath(Path path, int fd) {
            super(VirtualFileOperations.this, fd);
            this.path = path;
        }

        public Path path() {
            return path;
        }
    }
}
