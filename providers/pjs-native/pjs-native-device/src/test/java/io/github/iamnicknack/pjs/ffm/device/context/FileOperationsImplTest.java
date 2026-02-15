package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class FileOperationsImplTest {

    @Nested
    @Disabled
    class RealOperationsTest {
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

    @Nested
    class FakeOperationsTest {

        @Test
        void canOpenCreateFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("open", FileOperationsImpl.Descriptors.OPEN_CREATE, args -> {
                                assertThat(args[0]).isInstanceOf(MemorySegment.class);
                                assertThat(args[1]).isEqualTo(FileOperations.Flags.O_CREAT);
                                assertThat(args[2]).isEqualTo(0644);
                                return 1;
                            }),
                    fileOperations -> fileOperations.openFd("tmp.txt", FileOperations.Flags.O_CREAT)
            );
        }

        @Test
        void canOpenFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("open", FileOperationsImpl.Descriptors.OPEN, args -> {
                                assertThat(args[0]).isInstanceOf(MemorySegment.class);
                                assertThat(args[1]).isEqualTo(FileOperations.Flags.O_RDONLY);
                                return 2;
                            }),
                    fileOperations -> fileOperations.openFd("tmp.txt", FileOperations.Flags.O_RDONLY)
            );
        }

        @Test
        void canCloseFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("close", FileOperationsImpl.Descriptors.CLOSE, args -> {
                                assertThat(args[0]).isEqualTo(2);
                                return 0;
                            }),
                    fileOperations -> fileOperations.close(2)
            );
        }

        @Test
        void canReadFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("read", FileOperationsImpl.Descriptors.READ, args -> {
                                ((MemorySegment)args[1]).copyFrom(MemorySegment.ofArray(new byte[] { 1, 2, 3 }));
                                return 3;
                            }),
                    fileOperations -> {
                        var buffer = new byte[3];
                        assertThat(fileOperations.read(0, buffer, 0, 3)).isEqualTo(3);
                        assertThat(buffer).containsExactly(1, 2, 3);
                    }
            );
        }

        @Test
        void canReadIntoOffset() {
            performTest(
                    builder -> builder
                            .addMethodCaller("read", FileOperationsImpl.Descriptors.READ, args -> {
                                ((MemorySegment)args[1]).copyFrom(MemorySegment.ofArray(new byte[] { 3 }));
                                return 1;
                            }),
                    fileOperations -> {
                        var buffer = new byte[] { 0, 0, 0 };
                        assertThat(fileOperations.read(0, buffer, 1, 1)).isEqualTo(1);
                        assertThat(buffer).containsExactly(0, 3, 0);
                    }
            );
        }

        @Test
        void canReadEmptyFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("read", FileOperationsImpl.Descriptors.READ, args -> 0),
                    fileOperations ->
                            assertThat(fileOperations.read(0, new byte[0], 0, 0))
                                    .isEqualTo(0)
            );
        }

        @Test
        void canWriteToFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("write", FileOperationsImpl.Descriptors.WRITE, args -> {
                                var buffer = ((MemorySegment)args[1]).toArray(ValueLayout.JAVA_BYTE);
                                assertThat(buffer).containsExactly(1, 2, 3);
                                return 3;
                            }),
                    fileOperations ->
                            assertThat(fileOperations.write(0, new byte[] { 1, 2, 3 }, 0, 3))
                                    .isEqualTo(3)
            );
        }

        @Test
        void canWriteSliceToFile() {
            performTest(
                    builder -> builder
                            .addMethodCaller("write", FileOperationsImpl.Descriptors.WRITE, args -> {
                                var buffer = ((MemorySegment)args[1]).toArray(ValueLayout.JAVA_BYTE);
                                assertThat(buffer).containsExactly(2);
                                return 1;
                            }),
                    fileOperations ->
                            assertThat(fileOperations.write(0, new byte[] { 1, 2, 3 }, 1, 1))
                                    .isEqualTo(1)
            );
        }

        @Test
        void canCheckFileExists() {
            performTest(
                    builder -> builder
                            .addMethodCaller("access", FileOperationsImpl.Descriptors.ACCESS, args -> {
                                var memorySegment = (MemorySegment)args[0];
                                var filename = memorySegment.getString(0);
                                assertThat(filename).isEqualTo("tmp.txt");
                                assertThat(args[1]).isEqualTo(0);
                                return 1;
                            }),
                    fileOperations -> assertThat(fileOperations.exists("tmp.txt")).isFalse()
            );
        }

        @Test
        void canCheckFileIsValid() {
            performTest(
                    builder -> builder
                            .addMethodCaller("fcntl", FileOperationsImpl.Descriptors.FCNTL, args -> {
                                assertThat(args[0]).isEqualTo(2);
                                assertThat(args[1]).isEqualTo(1);
                                return 1;
                            })
                            .addMethodCaller("close", FileOperationsImpl.Descriptors.CLOSE, args -> {
                                assertThat(args[0]).isEqualTo(2);
                                return 0;
                            }),
                    fileOperations -> {
                        try (var fd = fileOperations.createFileDescriptor(2)) {
                            assertThat(fd.isValid()).isTrue();
                        }
                    }
            );
        }

        private void performTest(
                UnaryOperator<FakeNativeContext.Builder> configurer,
                Consumer<FileOperations> verifier
        ) {
            var context = configurer.apply(FakeNativeContext.builder()).build();
            var fileOperations = new FileOperationsImpl(context);
            verifier.accept(fileOperations);

            var methodCallerFactory = (FakeMethodCallerFactory)context.getMethodCallerFactory();
            methodCallerFactory.assertInvoked();
        }
    }

    static UnaryOperator<FakeNativeContext.Builder> defaultFileOperations(String filename) {
        return builder -> builder
                .addMethodCaller("open", FileOperationsImpl.Descriptors.OPEN, args -> {
                    assertThat(((MemorySegment)args[0]).getString(0)).isEqualTo(filename);
                    return 1;
                })
                .addMethodCaller("fcntl", FileOperationsImpl.Descriptors.FCNTL, args -> 1)
                .addMethodCaller("close", FileOperationsImpl.Descriptors.CLOSE, args -> 0);
    }
}