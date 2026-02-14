package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.foreign.Arena;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class FileOperationsImplTest {

    private final FileOperations fileOperations = new FileOperationsImpl(new DefaultNativeContext(Arena.ofAuto()));

    @AfterAll
    static void afterAll() {
        File file = new File("tmp.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void canRead() {
        createFile();

        try (var fd = fileOperations.openFd("tmp.txt", FileOperations.Flags.O_RDONLY);
             var in = fd.getInputStream()
        ) {
            byte[] buffer = new byte[100];
            int bytesRead = in.read(buffer);
            String content = new String(buffer, 0, bytesRead);
            assertThat(content).isEqualTo("Test File\n");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Test
    void canReadMemorySegment() {
        createFile();

        try (var fd = fileOperations.openFd("tmp.txt", FileOperations.Flags.O_RDONLY)) {
            var content = fileOperations.read(fd.fd(), 0, 100, (segment, ignored) ->
                segment.getString(0)
            );

            assertThat(content).isEqualTo("Test File\n");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void createFile() {
        var flags = FileOperations.Flags.O_CREAT |  FileOperations.Flags.O_WRONLY;
        try (var fd = fileOperations.openFd("tmp.txt", flags);
             var out = new PrintWriter(new OutputStreamWriter(fd.getOutputStream()))
        ) {
            out.println("Test File");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}