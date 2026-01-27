package io.github.iamnicknack.pjs.ffm.context.segment;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 * Writes a value of type T to a MemorySegment
 * @param <T> the type to serialize
 */
public interface MemorySegmentSerializer<T> extends WithLayout {

    /**
     * Writes a value of type T to a MemorySegment
     * @param data the value to serialize
     * @return the serialized segment
     */
    MemorySegment serialize(T data);

    /**
     * Writes a list of T to a MemorySegment
     * @param list the list to serialize
     * @param allocator the segment allocator to use
     * @return the serialized segment containing all elements of `list`
     */
    default MemorySegment serializeList(List<T> list, SegmentAllocator allocator) {
        var layout = layout();
        var segment = allocator.allocate(layout.byteSize() * list.size());
        for (int i = 0; i < list.size(); i++) {
            var slice = segment.asSlice(i * layout.byteSize(), layout.byteSize());
            var serialized = serialize(list.get(i));
            slice.copyFrom(serialized);
        }
        return segment;
    }

    /**
     * Writes a list of T to a MemorySegment
     * @param array the array to serialize
     * @param allocator the segment allocator to use
     * @return the serialized segment containing all elements of `list`
     */
    default MemorySegment serializeArray(T[] array, SegmentAllocator allocator) {
        var layout = layout();
        var segment = allocator.allocate(layout.byteSize() * array.length);
        for (int i = 0; i < array.length; i++) {
            var slice = segment.asSlice(i * layout.byteSize(), layout.byteSize());
            var serialized = serialize(array[i]);
            slice.copyFrom(serialized);
        }
        return segment;
    }

    /**
     * No-operation serializer that operates on MemorySegments
     */
    class Noop implements MemorySegmentSerializer<MemorySegment> {
        @Override
        public MemorySegment serialize(MemorySegment data) {
            return data;
        }

        @Override
        public MemoryLayout layout() {
            return ValueLayout.ADDRESS;
        }
    }
}
