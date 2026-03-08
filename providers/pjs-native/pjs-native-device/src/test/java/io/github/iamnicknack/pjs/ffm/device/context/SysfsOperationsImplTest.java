package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImplTest.defaultFileOperations;
import static org.assertj.core.api.Assertions.assertThat;

class SysfsOperationsImplTest {

    private static final String SYSFS_TEST = "sysfs-test";

    @Test
    void canWriteValue() {
        performTest(
                builder -> defaultFileOperations(SYSFS_TEST + "/test").apply(builder)
                        .addMethodCaller(
                                "write",
                                FileOperationsImpl.Descriptors.WRITE,
                                writeMethodCaller(new byte[] { 1, 2, 3 })
                        ),
                sysfsOperations -> {
                    sysfsOperations.write("test", new byte[] { 1, 2, 3 });
                }
        );
    }

    @Test
    void canReadValue() {
        performTest(
                builder -> defaultFileOperations(SYSFS_TEST + "/test").apply(builder)
                        .addMethodCaller(
                                "read",
                                FileOperationsImpl.Descriptors.READ,
                                readMethodCaller(new byte[] { 1, 2, 3 })
                        ),
                sysfsOperations -> {
                    var buffer = sysfsOperations.read("test");
                    assertThat(buffer).containsExactly(1, 2, 3);
                }
        );
    }

    @Test
    void canWriteString() {
        var value = "123";
        performTest(
                builder -> defaultFileOperations(SYSFS_TEST + "/test").apply(builder)
                        .addMethodCaller(
                                "write",
                                FileOperationsImpl.Descriptors.WRITE,
                                writeMethodCaller(value.getBytes())
                        ),
                sysfsOperations -> {
                    sysfsOperations.writeString("test", value);
                    sysfsOperations.writeLong("test", Long.parseLong(value));
                    sysfsOperations.writeInt("test", Integer.parseInt(value));
                }
        );
    }

    @Test
    void canReadString() {
        var value = "123";
        performTest(
                builder -> defaultFileOperations(SYSFS_TEST + "/test").apply(builder)
                        .addMethodCaller(
                                "read",
                                FileOperationsImpl.Descriptors.READ,
                                readMethodCaller(value.getBytes())
                        ),
                sysfsOperations -> {
                    assertThat(sysfsOperations.readString("test")).isEqualTo(value);
                    assertThat(sysfsOperations.readLong("test")).isEqualTo(Long.parseLong(value));
                    assertThat(sysfsOperations.readInt("test")).isEqualTo(Integer.parseInt(value));
                }
        );
    }

    private void performTest(
            UnaryOperator<FakeNativeContext.Builder> configurer,
            Consumer<SysfsOperations> verifier
    ) {
        var context = configurer.apply(FakeNativeContext.builder()).build();
        var fileOperations = new FileOperationsImpl(context);
        var sysfsOperations = new SysfsOperationsImpl(Path.of(SYSFS_TEST), fileOperations);
        verifier.accept(sysfsOperations);

        var methodCallerFactory = (FakeMethodCallerFactory)context.getMethodCallerFactory();
        methodCallerFactory.assertInvoked();
    }

    private MethodCaller readMethodCaller(byte[] data) {
        return args -> {
            ((MemorySegment)args[1]).copyFrom(MemorySegment.ofArray(data));
            return data.length;
        };
    }

    private MethodCaller writeMethodCaller(byte[] expectedData) {
        return args -> {
            var bytes = ((MemorySegment)args[1]).toArray(ValueLayout.JAVA_BYTE);
            assertThat(bytes).isEqualTo(expectedData);
            return 0;
        };
    }
}