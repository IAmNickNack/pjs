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

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

@SerializeUsing(ChipInfo.Serializer.class)
@DeserializeUsing(ChipInfo.Deserializer.class)
public record ChipInfo(
        String name,
        String label,
        int lines
) {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("name"),
            MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("label"),
            ValueLayout.JAVA_INT.withName("lines")
    );

    private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(groupElement("name"));
    private static final MethodHandle MH_LABEL = LAYOUT.sliceHandle(groupElement("label"));
    private static final VarHandle VH_LINES = LAYOUT.varHandle(groupElement("lines"));

    public String getPath() {
        return "/dev/" + name;
    }

    public static class Serializer implements MemorySegmentSerializer<ChipInfo> {
        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(ChipInfo data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);

                var nameSegment = (MemorySegment) MH_NAME.invoke(segment, 0L);
                nameSegment.setString(0L, data.name);

                var labelSegment = (MemorySegment) MH_LABEL.invoke(segment, 0L);
                labelSegment.setString(0L, data.label);

                VH_LINES.set(segment, 0L, data.lines());
                return segment;
            } catch (Throwable e) {
                throw new RuntimeException("Failed to serialize ChipInfo", e);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<ChipInfo> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public ChipInfo deserialize(MemorySegment segment) {
            try {
                var nameSegment = (MemorySegment) MH_NAME.invoke(segment, 0L);
                var name = nameSegment.getString(0L).trim();

                var labelSegment = (MemorySegment) MH_LABEL.invoke(segment, 0L);
                var label = labelSegment.getString(0L).trim();

                var lines = (int) VH_LINES.get(segment, 0L);

                return new ChipInfo(name, label, lines);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to deserialize ChipInfo", e);
            }
        }
    }
}
