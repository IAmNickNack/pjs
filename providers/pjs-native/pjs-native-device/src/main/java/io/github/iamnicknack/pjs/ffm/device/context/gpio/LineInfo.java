package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * @see <a href="https://docs.kernel.org/userspace-api/gpio/gpio-v2-get-lineinfo-ioctl.html">gpio-v2-get-lineinfo-ioctl</a>
 * @see <a href="https://github.com/torvalds/linux/blob/07d9df80082b8d1f37e05658371b087cb6738770/include/uapi/linux/gpio.h#L206-L232">linux/gpio.h</a>
 */
@SerializeUsing(LineInfo.Serializer.class)
@DeserializeUsing(LineInfo.Deserializer.class)
public record LineInfo(
        @Nullable String name,
        @Nullable String consumer,
        int offset,
        long flags,
        LineAttribute[] attrs
) {
    public static LineInfo ofOffset(int offset) {
        return new LineInfo(null, null, offset, 0L, new LineAttribute[0]);
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("name"),
        MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("consumer"),
        ValueLayout.JAVA_INT.withName("offset"),
        ValueLayout.JAVA_INT.withName("num_attrs"),
        ValueLayout.JAVA_LONG.withName("flags"),
        MemoryLayout.sequenceLayout(10, LineAttribute.LAYOUT).withName("attrs"),
        MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT).withName("padding")
    );

    private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(groupElement("name"));
    private static final MethodHandle MH_CONSUMER = LAYOUT.sliceHandle(groupElement("consumer"));
    private static final VarHandle VH_OFFSET = LAYOUT.varHandle(groupElement("offset"));
    private static final VarHandle VH_NUM_ATTRS = LAYOUT.varHandle(groupElement("num_attrs"));
    private static final VarHandle VH_FLAGS = LAYOUT.varHandle(groupElement("flags"));
    private static final MethodHandle MH_ATTRS = LAYOUT.sliceHandle(groupElement("attrs"));


    public static class Serializer implements MemorySegmentSerializer<LineInfo> {
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
        public MemorySegment serialize(LineInfo data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);

                var nameSegment = (MemorySegment) MH_NAME.invoke(segment, 0L);
                if (data.name != null) {
                    nameSegment.setString(0L, data.name);
                }

                var consumerSegment = (MemorySegment) MH_CONSUMER.invoke(segment, 0L);
                if (data.consumer != null) {
                    consumerSegment.setString(0L, data.consumer);
                }

                VH_OFFSET.set(segment, 0L, data.offset);
                VH_NUM_ATTRS.set(segment, 0L, data.attrs.length);
                VH_FLAGS.set(segment, 0L, data.flags);

                var attrsSegment = (MemorySegment) MH_ATTRS.invoke(segment, 0L);
                for (int i = 0; i < data.attrs.length; i++) {
                    var attrSlice = attrsSegment.asSlice(i * LineAttribute.LAYOUT.byteSize(), LineAttribute.LAYOUT.byteSize());
                    var attrSegment = lineAttributeSerializer.serialize(data.attrs[i]);
                    attrSlice.copyFrom(attrSegment);
                }
                return segment;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<LineInfo> {

        private final LineAttribute.Deserializer lineAttributeDeserializer = new LineAttribute.Deserializer();

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public LineInfo deserialize(MemorySegment segment) {
            try {
                var nameSegment = (MemorySegment) MH_NAME.invoke(segment, 0L);
                var name = nameSegment.getString(0).trim();

                var consumerSegment = (MemorySegment) MH_CONSUMER.invoke(segment, 0L);
                var consumer = consumerSegment.getString(0).trim();

                var offset = (int) VH_OFFSET.get(segment, 0L);
                var numAttrs = (int) VH_NUM_ATTRS.get(segment, 0L);
                var flags = (long) VH_FLAGS.get(segment, 0L);

                var attrs = new LineAttribute[numAttrs];
                var attrsSegment = (MemorySegment) MH_ATTRS.invoke(segment, 0L);
                for (int i = 0; i < numAttrs; i++) {
                    var attrSegment = attrsSegment.asSlice(i * LineAttribute.LAYOUT.byteSize(), LineAttribute.LAYOUT.byteSize());
                    attrs[i] = lineAttributeDeserializer.deserialize(attrSegment);
                }
                return new LineInfo(name, consumer, offset, flags, attrs);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "LineInfo{" +
            "name=" + name +
            ", consumer=" + consumer +
            ", offset=" + offset +
            ", flags=" + flags +
            ", attrs=" + Arrays.toString(attrs) +
            '}';
    }
}
