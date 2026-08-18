package io.github.iamnicknack.pjs.ffm.context;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class NativeContextTest {

    @Test
    void testIsAvailable() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem()) {
            createFileWithContent(fs, "raspberry pi");

            boolean available = NativeContext.isAvailable(fs);
            assertThat(available).isTrue();
        }
    }

    @Test
    void testNonPi() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem()) {
            createFileWithContent(fs, "other");

            boolean available = NativeContext.isAvailable(fs);
            assertThat(available).isFalse();
        }
    }

    @Test
    void testNonLinux() {
        boolean available = NativeContext.isAvailable();
        assertThat(available).isFalse();
    }


    private void createFileWithContent(FileSystem fs, String content) throws IOException {
        var path = fs.getPath("/proc/cpuinfo");
        var txt = "model " + content;

        Files.createDirectory(path.getParent());
        Files.createFile(path);
        Files.write(path, txt.getBytes());
    }
}