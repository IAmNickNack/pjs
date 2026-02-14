package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.assertj.core.api.Assertions.assertThat;

class PollTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void canSerialise() {
        var poll = new Poll(1, 2, 3);
        var segment = mapper.segment(poll);
        var deserialisedPoll = mapper.value(segment, Poll.class);

        assertThat(deserialisedPoll).isEqualTo(poll);

    }

}