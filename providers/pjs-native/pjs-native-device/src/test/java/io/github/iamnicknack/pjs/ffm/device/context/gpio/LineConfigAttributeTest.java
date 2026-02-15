package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

class LineConfigAttributeTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canSerialiseAttribute() {
        var lineAttribute = new LineAttribute(LineAttribute.Id.VALUES, 3);
        var attribute = new LineConfigAttribute(lineAttribute, 4);
        var segment = mapper.segment(attribute);

        var bytes = segment.toArray(ValueLayout.JAVA_BYTE);
        assert bytes.length == LineConfigAttribute.LAYOUT.byteSize();

        var deserialisedAttribute = mapper.value(segment, LineConfigAttribute.class);
        assert attribute.equals(deserialisedAttribute);
    }
}