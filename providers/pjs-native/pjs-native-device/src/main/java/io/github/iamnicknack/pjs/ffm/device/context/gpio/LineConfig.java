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
 * @see <a href="https://github.com/torvalds/linux/blob/07d9df80082b8d1f37e05658371b087cb6738770/include/uapi/linux/gpio.h#L153-L173">linux/gpio.h</a>
 */
@SerializeUsing(LineConfig.Serializer.class)
@DeserializeUsing(LineConfig.Deserializer.class)
public record LineConfig(
        long flags,
        LineConfigAttribute[] attributes
) {
    public static LineConfig ofFlags(long flags) {
        return new LineConfig(flags, new LineConfigAttribute[0]);
    }

    @Override
    public String toString() {
        return "LineConfig{" +
                "flags=" + flags +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("flags"),
            ValueLayout.JAVA_INT.withName("num_attrs"),
            MemoryLayout.sequenceLayout(5, ValueLayout.JAVA_INT).withName("padding"),
            MemoryLayout.sequenceLayout(10, LineConfigAttribute.LAYOUT).withName("attrs")
    );

    private static final VarHandle VH_FLAGS = LAYOUT.varHandle(groupElement("flags"));
    private static final VarHandle VH_NUM_ATTRS = LAYOUT.varHandle(groupElement("num_attrs"));
    private static final MethodHandle MH_ATTRS = LAYOUT.sliceHandle(groupElement("attrs"));


    public static class Serializer implements MemorySegmentSerializer<LineConfig> {

        private final SegmentAllocator segmentAllocator;
        private final LineConfigAttribute.Serializer attributeSerializer;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
            this.attributeSerializer = new LineConfigAttribute.Serializer(segmentAllocator);
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(LineConfig data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);

                VH_FLAGS.set(segment, 0L, data.flags);
                VH_NUM_ATTRS.set(segment, 0, data.attributes.length);

                var attrsSegment = (MemorySegment) MH_ATTRS.invoke(segment, 0L);
                for (int i = 0; i < data.attributes.length; i++) {
                    var attrSlice = attrsSegment.asSlice(i * LineConfigAttribute.LAYOUT.byteSize(), LineConfigAttribute.LAYOUT.byteSize());
                    var attrSegment = attributeSerializer.serialize(data.attributes[i]);
                    attrSlice.copyFrom(attrSegment);
                }

                return segment;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineConfig> {
        private final LineConfigAttribute.Deserializer attributeDeserializer;

        public Deserializer() {
            this.attributeDeserializer = new LineConfigAttribute.Deserializer();
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineConfig deserialize(MemorySegment segment) {
            try {
                long flags = (long) VH_FLAGS.get(segment, 0L);
                int numAttributes = (int) VH_NUM_ATTRS.get(segment, 0);

                var attrsSegment = (MemorySegment) MH_ATTRS.invoke(segment, 0L);
                LineConfigAttribute[] attributes = new LineConfigAttribute[numAttributes];
                for (int i = 0; i < numAttributes; i++) {
                    var attrSlice = attrsSegment.asSlice(i * LineConfigAttribute.LAYOUT.byteSize(), LineConfigAttribute.LAYOUT.byteSize());
                    attributes[i] = attributeDeserializer.deserialize(attrSlice);
                }

                return new LineConfig(flags, attributes);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
