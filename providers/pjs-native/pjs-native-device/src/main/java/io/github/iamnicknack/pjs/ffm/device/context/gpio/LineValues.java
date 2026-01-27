package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * Values of GPIO lines
 * @param bits a bitmap containing the value of the lines, set to 1 for active and 0 for inactive.
 * @param mask a bitmap identifying the lines to get or set, with each bit number corresponding to
 *             the index into &struct gpio_v2_line_request.offsets.
 * @see <a href="https://github.com/torvalds/linux/blob/07d9df80082b8d1f37e05658371b087cb6738770/include/uapi/linux/gpio.h#L88-L99">linux/gpio.h</a>
 */
@SerializeUsing(LineValues.Serializer.class)
@DeserializeUsing(LineValues.Deserializer.class)
public record LineValues(
        long bits,
        long mask
) {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("bits"),
            ValueLayout.JAVA_LONG.withName("mask")
    );

    private static final VarHandle VH_BITS = LAYOUT.varHandle(groupElement("bits"));
    private static final VarHandle VH_MASK = LAYOUT.varHandle(groupElement("mask"));

    public static class Serializer implements MemorySegmentSerializer<LineValues> {
        private final java.lang.foreign.SegmentAllocator segmentAllocator;

        public Serializer(java.lang.foreign.SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public java.lang.foreign.MemorySegment serialize(LineValues data) {
            var segment = segmentAllocator.allocate(LAYOUT);
            VH_BITS.set(segment, 0L, data.bits);
            VH_MASK.set(segment, 0L, data.mask);
            return segment;
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineValues> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineValues deserialize(java.lang.foreign.MemorySegment segment) {
            var bits = (long) VH_BITS.get(segment, 0L);
            var mask = (long) VH_MASK.get(segment, 0L);
            return new LineValues(bits, mask);
        }
    }
}
