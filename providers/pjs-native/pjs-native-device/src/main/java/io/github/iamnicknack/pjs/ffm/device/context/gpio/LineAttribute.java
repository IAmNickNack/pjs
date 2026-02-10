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
 * Line attribute structure.
 * @param id the attribute id / type
 * @param value the value of the attribute
 * @see <a href="https://docs.kernel.org/userspace-api/gpio/chardev.html#c.gpio_v2_line_attribute">gpio_v2_line_attribute</a>
 * @see <a href="https://github.com/torvalds/linux/blob/master/include/uapi/linux/gpio.h#L130-L138">gpio_v2_line_attribute (GitHub)</a>
 */
@SerializeUsing(LineAttribute.Serializer.class)
@DeserializeUsing(LineAttribute.Deserializer.class)
public record LineAttribute(
        Id id,
        long value
) {
    /**
     * Valid attribute ids.
     */
    public enum Id {
        FLAGS(1),
        VALUES(2),
        DEBOUNCE_PERIOD_US(3);

        private final int id;

        Id(int id) {
            this.id = id;
        }

        static Id fromId(int id) {
            return switch (id) {
                case 1 -> FLAGS;
                case 2 -> VALUES;
                case 3 -> DEBOUNCE_PERIOD_US;
                default -> throw new IllegalArgumentException("Unknown LineAttributeId id: " + id);
            };
        }
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            ValueLayout.JAVA_INT.withName("padding"),
            MemoryLayout.unionLayout(
                    ValueLayout.JAVA_LONG.withName("flags"),
                    ValueLayout.JAVA_LONG.withName("values"),
                    ValueLayout.JAVA_INT.withName("debounce_period_us")
            ).withName("union")
    );

    private static final VarHandle VH_ID = LAYOUT.varHandle(groupElement("id"));
    private static final VarHandle VH_FLAGS = LAYOUT.varHandle(
            groupElement("union"),
            groupElement("flags")
    );
    private static final VarHandle VH_VALUES = LAYOUT.varHandle(
            groupElement("union"),
            groupElement("values")
    );
    private static final VarHandle VH_DEBOUNCE = LAYOUT.varHandle(
            groupElement("union"),
            groupElement("debounce_period_us")
    );

    public static class Serializer implements MemorySegmentSerializer<LineAttribute> {

        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(LineAttribute data) {
            var segment = segmentAllocator.allocate(LAYOUT);
            VH_ID.set(segment, 0L, data.id.id);
            switch (data.id) {
                case FLAGS -> VH_FLAGS.set(segment, 0L, data.value);
                case VALUES -> VH_VALUES.set(segment, 0L, data.value);
                case DEBOUNCE_PERIOD_US -> VH_DEBOUNCE.set(segment, 0L, (int)data.value);
            }
            return segment;
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineAttribute> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineAttribute deserialize(MemorySegment segment) {
            var id = Id.fromId((int) VH_ID.get(segment, 0L));
            return new LineAttribute(
                    id,
                    switch (id) {
                        case FLAGS -> (long) VH_FLAGS.get(segment, 0L);
                        case VALUES -> (long) VH_VALUES.get(segment, 0L);
                        case DEBOUNCE_PERIOD_US -> (int)VH_DEBOUNCE.get(segment, 0L);
                    }
            );
        }
    }

    @Override
    public String toString() {
        return "LineAttribute{" +
            "id=" + id +
            ", value=" + value +
            '}';
    }
}
