package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

import static org.assertj.core.api.Assertions.assertThat;

class LineEventTest {

    private final SegmentAllocator allocator = Arena.ofAuto();
    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(allocator);


    @Test
    void canWriteRead() {
        var lineEvent = new LineEvent(1, 2, 3, 4, 5);
        var segment = mapper.segment(lineEvent);
        var deserialisedLineEvent = mapper.value(segment, LineEvent.class);

        assertThat(deserialisedLineEvent).isEqualTo(lineEvent);
    }

    @Test
    void canSerialiseToBytes() {
        var lineEvent = new LineEvent(1, 2, 3, 4, 5);
        var segment = mapper.segment(lineEvent);
        var bytes = segment.toArray(ValueLayout.JAVA_BYTE);

        assertThat(bytes.length).isEqualTo(LineEvent.LAYOUT.byteSize());

        var bytesSegment = allocator.allocateFrom(ValueLayout.JAVA_BYTE, bytes);
        var deserialisedLineEvent = mapper.value(bytesSegment, LineEvent.class);

        assertThat(deserialisedLineEvent).isEqualTo(lineEvent);
    }

}