package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

class LineAttributeTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canSerialiseFlags() {
        var attr = new LineAttribute(LineAttribute.Id.FLAGS, 1);
        var segment = mapper.segment(attr);
        var deserialisedAttr = mapper.value(segment, LineAttribute.class);
        assert attr.equals(deserialisedAttr);
    }

    @Test
    void canSerialiseValues() {
        var attr = new LineAttribute(LineAttribute.Id.VALUES, 7);
        var segment = mapper.segment(attr);
        var deserialisedAttr = mapper.value(segment, LineAttribute.class);
        assert attr.equals(deserialisedAttr);
    }

    @Test
    void canSerialiseDebouncePeriod() {
        var attr = new LineAttribute(LineAttribute.Id.DEBOUNCE_PERIOD_US, 50_000);
        var segment = mapper.segment(attr);
        var deserialisedAttr = mapper.value(segment, LineAttribute.class);
        assert attr.equals(deserialisedAttr);
    }


}