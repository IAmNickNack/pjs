package io.github.iamnicknack.pjs.ffm.context.segment;

import org.jspecify.annotations.Nullable;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemorySegmentMapperImpl implements MemorySegmentMapper {

    private final SegmentAllocator segmentAllocator;
    private final Map<Class<?>, MemorySegmentSerializer<?>> serializerMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, MemorySegmentDeserializer<?>> deserializerMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, MemoryLayout> layoutMap = new ConcurrentHashMap<>();

    public MemorySegmentMapperImpl(SegmentAllocator segmentAllocator) {
        this.segmentAllocator = segmentAllocator;
        this.registerSerializer(MemorySegment.class, new MemorySegmentSerializer.Noop());
        this.registerDeserializer(MemorySegment.class, new MemorySegmentDeserializer.Noop());
    }

    @SuppressWarnings("unused")
    public MemorySegmentMapperImpl registerSerializer(Class<?> type, MemorySegmentSerializer<?> serializer) {
        serializerMap.put(type, serializer);
        layoutMap.put(type, serializer.layout());
        return this;
    }

    @SuppressWarnings("unused")
    public MemorySegmentMapperImpl registerDeserializer(Class<?> type, MemorySegmentDeserializer<?> deserializer) {
        deserializerMap.put(type, deserializer);
        layoutMap.put(type, deserializer.layout());
        return this;
    }

    @SuppressWarnings("unused")
    public MemorySegmentMapperImpl registerLayout(Class<?> type, MemoryLayout layout) {
        layoutMap.put(type, layout);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T value(MemorySegment segment, Class<T> targetType) {
        return Optional.ofNullable(deserializerMap.computeIfAbsent(targetType, this::findDeserializer))
                .map(deserializer -> (T)deserializer.deserialize(segment))
                .orElseThrow(() -> new IllegalArgumentException("No deserializer registered for type: " + targetType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MemorySegment segment(T value, Class<T> sourceType) {
        return Optional.ofNullable(serializerMap.computeIfAbsent(sourceType, this::findSerializer))
                .map(serializer -> ((MemorySegmentSerializer<T>)serializer).serialize(value))
                .orElseThrow(() -> new IllegalArgumentException("No serializer registered for type: " + sourceType));
    }

    @Override
    public MemoryLayout layout(Class<?> type) {
        return layoutMap.computeIfAbsent(
                type,
                t -> Optional.ofNullable((WithLayout)this.findSerializer(t))
                        .or(() -> Optional.ofNullable((WithLayout)this.findDeserializer(t)))
                        .map(WithLayout::layout)
                        .orElseThrow(() -> new IllegalArgumentException("type has no memory layout"))
        );
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> MemorySegmentSerializer<T> findSerializer(Class<T> type) {
        var annotation = type.getAnnotation(SerializeUsing.class);
        if (annotation != null) {
            try {
                var constructor = annotation.value().getDeclaredConstructor(SegmentAllocator.class);
                return (MemorySegmentSerializer<T>) constructor.newInstance(segmentAllocator);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate serializer for type: " + type, e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> MemorySegmentDeserializer<T> findDeserializer(Class<T> type) {
        var annotation = type.getAnnotation(DeserializeUsing.class);
        if (annotation != null) {
            try {
                var constructor = annotation.value().getDeclaredConstructor();
                return (MemorySegmentDeserializer<T>)constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate deserializer for type: " + type, e);
            }
        }
        return null;
    }
}
