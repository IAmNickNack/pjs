package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class IoctlOperationsImplTest {

    @Test
    void canCallIoctlWithIntData() {
        performTest(
                builder -> builder
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            assertThat(args).hasSize(3);
                            assertThat(args[0]).isEqualTo(1);
                            assertThat(args[1]).isEqualTo(2L);
                            assertThat((MemorySegment)args[2]).matches(segment -> {
                                assertThat(segment.byteSize()).isEqualTo(ValueLayout.JAVA_INT.byteSize());
                                assertThat(segment.get(ValueLayout.JAVA_INT, 0)).isEqualTo(3);
                                return true;
                            });
                            return 0;
                        }),
                ioctlOperations -> {
                    ioctlOperations.ioctl(1, 2, 3);
                }
        );
    }

    @Test
    void canCallIoctlWithJavaObject() {
        performTest(
                builder -> builder
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            assertThat(args[0]).isEqualTo(1);
                            assertThat(args[1]).isEqualTo(2L);
                            assertThat((MemorySegment)args[2]).matches(segment -> {
                                assertThat(segment.byteSize()).isEqualTo(ValueLayout.JAVA_INT.byteSize());
                                assertThat(segment.get(ValueLayout.JAVA_INT, 0)).isEqualTo(3);
                                return true;
                            });
                            return 0;
                        }),
                ioctlOperations -> ioctlOperations.ioctl(1, 2, new TestObject(3))
        );
    }

    @Test
    void canCallIoctlForObjectResult() {
        performTest(
                builder -> builder
                        .addMethodCaller("ioctl", IoctlOperationsImpl.Descriptors.IOCTL_INT_BY_REFERENCE, args -> {
                            var segment = (MemorySegment)args[2];
                            segment.set(ValueLayout.JAVA_INT, 0, 3);
                            return 0;
                        }),
                ioctlOperations -> assertThat(ioctlOperations.ioctl(1, 2, TestObject.class))
                        .isEqualTo(new TestObject(3))
        );
    }


    private void performTest(
            UnaryOperator<FakeNativeContext.Builder> configurer,
            Consumer<IoctlOperations> verifier
    ) {
        var context = configurer.apply(FakeNativeContext.builder()).build();
        var fileOperations = new IoctlOperationsImpl(context);
        verifier.accept(fileOperations);

        var methodCallerFactory = (FakeMethodCallerFactory)context.getMethodCallerFactory();
        methodCallerFactory.assertInvoked();
    }

    @SerializeUsing(TestObject.Serializer.class)
    @DeserializeUsing(TestObject.Deserializer.class)
    public record TestObject(int value) {

        public static class Serializer implements MemorySegmentSerializer<TestObject> {

            private final SegmentAllocator segmentAllocator;

            public Serializer(SegmentAllocator segmentAllocator) {
                this.segmentAllocator = segmentAllocator;
            }

            @Override
            public MemorySegment serialize(TestObject data) {
                var segment = segmentAllocator.allocate(ValueLayout.JAVA_INT.byteSize());
                segment.set(ValueLayout.JAVA_INT, 0, data.value);
                return segment;
            }

            @Override
            public MemoryLayout layout() {
                return ValueLayout.JAVA_INT;
            }
        }

        public static class Deserializer implements MemorySegmentDeserializer<TestObject> {
            @Override
            public MemoryLayout layout() {
                return ValueLayout.JAVA_INT;
            }
            @Override
            public TestObject deserialize(MemorySegment segment) {
                return new TestObject(segment.get(ValueLayout.JAVA_INT, 0));
            }
        }
    }
}