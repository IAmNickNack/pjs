package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * @see <a href="https://docs.kernel.org/userspace-api/gpio/chardev.html#c.gpio_v2_line_event">gpio_v2_line_event</a>
 * @param timestampNs the timestamp of the event
 * @param id event id (rising / falling)
 * @param offset the line which triggered the event
 * @param seqno
 * @param lineSeqno
 */
@SerializeUsing(LineEvent.Serializer.class)
@DeserializeUsing(LineEvent.Deserializer.class)
public record LineEvent(
        long timestampNs,
        int id,
        int offset,
        int seqno,
        int lineSeqno
) {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("timestamp_ns"),
            ValueLayout.JAVA_INT.withName("id"),
            ValueLayout.JAVA_INT.withName("offset"),
            ValueLayout.JAVA_INT.withName("seqno"),
            ValueLayout.JAVA_INT.withName("line_seqno"),
            MemoryLayout.sequenceLayout(6, ValueLayout.JAVA_INT).withName("padding")
    );

    private static final VarHandle VH_TIMESTAMP_NS = LAYOUT.varHandle(groupElement("timestamp_ns"));
    private static final VarHandle VH_ID = LAYOUT.varHandle(groupElement("id"));
    private static final VarHandle VH_OFFSET = LAYOUT.varHandle(groupElement("offset"));
    private static final VarHandle VH_SEQNO = LAYOUT.varHandle(groupElement("seqno"));
    private static final VarHandle VH_LINE_SEQNO = LAYOUT.varHandle(groupElement("line_seqno"));


    public static class Serializer implements MemorySegmentSerializer<LineEvent> {
        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(LineEvent data) {
            var segment = segmentAllocator.allocate(LAYOUT);
            VH_TIMESTAMP_NS.set(segment, 0L, data.timestampNs);
            VH_ID.set(segment, 0L, data.id);
            VH_OFFSET.set(segment, 0L, data.offset);
            VH_SEQNO.set(segment, 0L, data.seqno);
            VH_LINE_SEQNO.set(segment, 0L, data.lineSeqno);
            return segment;
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineEvent> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineEvent deserialize(MemorySegment segment) {
            var timestampNs = (long) VH_TIMESTAMP_NS.get(segment, 0L);
            var id = (int) VH_ID.get(segment, 0L);
            var offset = (int) VH_OFFSET.get(segment, 0L);
            var seqno = (int) VH_SEQNO.get(segment, 0L);
            var lineSeqno = (int) VH_LINE_SEQNO.get(segment, 0L);
            return new LineEvent(timestampNs, id, offset, seqno, lineSeqno);
        }
    }
}
