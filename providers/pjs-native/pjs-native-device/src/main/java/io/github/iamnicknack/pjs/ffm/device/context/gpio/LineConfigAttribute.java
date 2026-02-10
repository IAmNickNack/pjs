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

/**
 * @see <a href="https://github.com/torvalds/linux/blob/master/include/uapi/linux/gpio.h#L148-L151>gpio_v2_line_config_attribute (GitHub))</a>
 */
@SerializeUsing(LineConfigAttribute.Serializer.class)
@DeserializeUsing(LineConfigAttribute.Deserializer.class)
public record LineConfigAttribute(
        LineAttribute lineAttribute,
        long mask
) {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            LineAttribute.LAYOUT.withName("lineAttribute"),
            ValueLayout.JAVA_LONG.withName("mask")
    );

    private static final long LINE_ATTRIBUTE_OFFSET = LAYOUT.byteOffset(
            MemoryLayout.PathElement.groupElement("lineAttribute")
    );

    private static final VarHandle MASK_HANDLE = LAYOUT.varHandle(
            MemoryLayout.PathElement.groupElement("mask")
    );


    public static class Serializer implements MemorySegmentSerializer<LineConfigAttribute> {
        private final SegmentAllocator segmentAllocator;
        private final LineAttribute.Serializer lineAttributeSerializer;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
            this.lineAttributeSerializer = new LineAttribute.Serializer(segmentAllocator);
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(LineConfigAttribute data) {
            var segment = segmentAllocator.allocate(LAYOUT);
            var lineAttrSegment = lineAttributeSerializer.serialize(data.lineAttribute);
            MemorySegment.copy(
                    lineAttrSegment, 0,
                    segment, LINE_ATTRIBUTE_OFFSET,
                    LineAttribute.LAYOUT.byteSize()
            );
            MASK_HANDLE.set(segment, 0L, data.mask);
            return segment;
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineConfigAttribute> {
        private final LineAttribute.Deserializer lineAttributeDeserializer;

        public Deserializer() {
            this.lineAttributeDeserializer = new LineAttribute.Deserializer();
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineConfigAttribute deserialize(MemorySegment segment) {
            var lineAttrSegment = segment.asSlice(LINE_ATTRIBUTE_OFFSET, LineAttribute.LAYOUT.byteSize());
            var lineAttribute = lineAttributeDeserializer.deserialize(lineAttrSegment);
            var mask = (long) MASK_HANDLE.get(segment, 0L);
            return new LineConfigAttribute(lineAttribute, mask);
        }
    }

}
