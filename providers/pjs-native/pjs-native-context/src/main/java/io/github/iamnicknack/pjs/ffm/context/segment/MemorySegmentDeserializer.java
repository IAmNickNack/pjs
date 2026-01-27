package io.github.iamnicknack.pjs.ffm.context.segment;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 * Reads a value of type T from a MemorySegment
 * @param <T> the type to deserialize
 */
public interface MemorySegmentDeserializer<T> extends WithLayout {
    /**
     * Reads a value of type T from the given MemorySegment
     * @param segment the segment to read from
     * @return the deserialized value
     */
    T deserialize(MemorySegment segment);

    /**
     * Reads a list of T from the given MemorySegment
     * @param segment the segment to read from
     * @param count the number of elements to read
     * @return the deserialized list
     */
    default List<T> deserializeList(MemorySegment segment, int count) {
        var layout = layout();
        var list = new java.util.ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            var slice = segment.asSlice(i * layout.byteSize(), layout.byteSize());
            list.add(deserialize(slice));
        }
        return list;
    }

    /**
     * No-operation deserializer that returns the segment as-is
     */
    class Noop implements MemorySegmentDeserializer<MemorySegment> {
        @Override
        public MemorySegment deserialize(MemorySegment segment) {
            return segment;
        }

        @Override
        public MemoryLayout layout() {
            return ValueLayout.ADDRESS;
        }
    }

    /**
     * Factory method for deserializers with hints
     * @param <T> the type to deserialize
     * @param <V> the hint type
     */
    @FunctionalInterface
    interface Factory<T, V> {
        MemorySegmentDeserializer<T> create(V hint);
    }
}
