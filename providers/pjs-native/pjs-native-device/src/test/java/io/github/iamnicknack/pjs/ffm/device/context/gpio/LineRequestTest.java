package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

class LineRequestTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canWriteRead() {

        var original = new LineRequest(
                new int[]{1, 2, 3},
                "consumer",
                new LineConfig(5, new LineConfigAttribute[0]),
                16,
                42
        );

        var segment = mapper.segment(original, LineRequest.class);
        var restored = mapper.value(segment, LineRequest.class);

        Assertions.assertThat(restored.offsets()).containsExactly(original.offsets());
        Assertions.assertThat(restored.consumer()).isEqualTo(original.consumer());
        Assertions.assertThat(restored.config().flags()).isEqualTo(original.config().flags());
        Assertions.assertThat(restored.eventBufferSize()).isEqualTo(original.eventBufferSize());
        Assertions.assertThat(restored.fd()).isEqualTo(original.fd());
    }
}