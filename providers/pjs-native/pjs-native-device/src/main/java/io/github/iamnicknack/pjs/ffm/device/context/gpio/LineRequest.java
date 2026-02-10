package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * @see <a href="https://github.com/torvalds/linux/blob/master/include/uapi/linux/gpio.h#L195-L204">gpio_v2_line_request (GitHub)</a>
 */
@SerializeUsing(LineRequest.Serializer.class)
@DeserializeUsing(LineRequest.Deserializer.class)
public record LineRequest(
        int[] offsets,
        String consumer,
        LineConfig config,
        int eventBufferSize,
        int fd
) {

    @Override
    public String toString() {
        return "LineRequest{" +
                "offsets=" + Arrays.toString(offsets) +
                ", consumer='" + consumer + '\'' +
                ", config=" + config +
                ", eventBufferSize=" + eventBufferSize +
                ", fd=" + fd +
                '}';
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.sequenceLayout(64, ValueLayout.JAVA_INT).withName("offsets"),
            MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("consumer"),
            LineConfig.LAYOUT.withName("config"),
            ValueLayout.JAVA_INT.withName("num_lines"),
            ValueLayout.JAVA_INT.withName("event_buffer_size"),
            MemoryLayout.sequenceLayout(5, ValueLayout.JAVA_INT).withName("padding"),
            ValueLayout.JAVA_INT.withName("fd")
    );

    private static final MethodHandle MH_OFFSETS = LAYOUT.sliceHandle(groupElement("offsets"));
    private static final MethodHandle MH_CONSUMER = LAYOUT.sliceHandle(groupElement("consumer"));
    private static final MethodHandle MH_CONFIG = LAYOUT.sliceHandle(groupElement("config"));
    private static final VarHandle VH_NUM_LINES = LAYOUT.varHandle(groupElement("num_lines"));
    private static final VarHandle VH_EVENT_BUFFER_SIZE = LAYOUT.varHandle(groupElement("event_buffer_size"));
    private static final VarHandle VH_FD = LAYOUT.varHandle(groupElement("fd"));


    public static class Serializer implements MemorySegmentSerializer<LineRequest> {
        private final SegmentAllocator segmentAllocator;
        private final LineConfig.Serializer lineConfigSerializer;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
            this.lineConfigSerializer = new LineConfig.Serializer(segmentAllocator);
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(LineRequest data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);

                var offsetsSegment = (MemorySegment) MH_OFFSETS.invoke(segment, 0L);
                for (int i = 0; i < data.offsets.length; i++) {
                    offsetsSegment.setAtIndex(ValueLayout.JAVA_INT, i, data.offsets[i]);
                }

                var consumerSegment = (MemorySegment) MH_CONSUMER.invoke(segment, 0L);
                consumerSegment.setString(0L, data.consumer);

                var configSegment = (MemorySegment) MH_CONFIG.invoke(segment, 0L);
                var serializedConfig = lineConfigSerializer.serialize(data.config);
                configSegment.copyFrom(serializedConfig);

                VH_NUM_LINES.set(segment, 0, data.offsets.length);
                VH_EVENT_BUFFER_SIZE.set(segment, 0, data.eventBufferSize);
                VH_FD.set(segment, 0, data.fd);

                return segment;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineRequest> {
        private final LineConfig.Deserializer lineConfigDeserializer;

        public Deserializer() {
            this.lineConfigDeserializer = new LineConfig.Deserializer();
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineRequest deserialize(MemorySegment segment) {
            try {
                var numLines = (int) VH_NUM_LINES.get(segment, 0L);
                var offsets = new int[numLines];
                var offsetsSegment = (MemorySegment) MH_OFFSETS.invoke(segment, 0L);
                for (int i = 0; i < numLines; i++) {
                    offsets[i] = offsetsSegment.getAtIndex(ValueLayout.JAVA_INT, i);
                }

                var consumerSegment = (MemorySegment) MH_CONSUMER.invoke(segment, 0L);
                var consumer = consumerSegment.getString(0L);

                var configSegment = (MemorySegment) MH_CONFIG.invoke(segment, 0L);
                var config = lineConfigDeserializer.deserialize(configSegment);

                var eventBufferSize = (int) VH_EVENT_BUFFER_SIZE.get(segment, 0L);
                var fd = (int) VH_FD.get(segment, 0L);

                return new LineRequest(offsets, consumer, config, eventBufferSize, fd);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
