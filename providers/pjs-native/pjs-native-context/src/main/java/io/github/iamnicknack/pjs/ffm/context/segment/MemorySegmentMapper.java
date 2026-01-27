package io.github.iamnicknack.pjs.ffm.context.segment;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public interface MemorySegmentMapper {
    /**
     * Read a value from a MemorySegment and convert it to the specified target type.
     * @param segment the MemorySegment to read from
     * @param targetType the class of the target type
     * @return the converted value
     * @param <T> the target type
     */
    <T> T convertValue(MemorySegment segment, Class<T> targetType);

    /**
     * Write a value to a MemorySegment, converting it from the specified source type.
     * @param value the value to write
     * @param sourceType the class of the source type
     * @return the MemorySegment representation of the value
     */
    <T> MemorySegment segment(T value, Class<T> sourceType);

    /**
     * Get the MemoryLayout for a given type.
     * @param type the class of the type
     * @return the MemoryLayout
     */
    MemoryLayout layout(Class<?> type);

    @SuppressWarnings("unchecked")
    default <T> MemorySegment segment(T value) {
        return segment(value, (Class<T>)value.getClass());
    }
}
