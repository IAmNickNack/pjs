package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

class LineConfigTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canSerialiseLineConfig() {
        var attr = new LineAttribute(LineAttribute.Id.DEBOUNCE_PERIOD_US, 50_000);
        var configAttribute = new LineConfigAttribute(attr, 1 << 15);
        var config = new LineConfig(1, new LineConfigAttribute[] { configAttribute });
        var segment = mapper.segment(config);
        var deserialisedConfig = mapper.value(segment, LineConfig.class);
        Assertions.assertThat(deserialisedConfig).isEqualTo(config);
    }
}