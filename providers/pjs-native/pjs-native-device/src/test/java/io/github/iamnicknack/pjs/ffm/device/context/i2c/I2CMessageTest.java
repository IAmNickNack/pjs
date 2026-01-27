package io.github.iamnicknack.pjs.ffm.device.context.i2c;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.assertj.core.api.Assertions.assertThat;

class I2CMessageTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void test() {
        var message = new I2CMessage(1, 2, 3, new byte[] { 1, 2, 3 });
        var segment = mapper.segment(message);
        var deserializedMessage = mapper.convertValue(segment, I2CMessage.class);

        assertEqual(message, deserializedMessage);
    }

    public static void assertEqual(I2CMessage expected, I2CMessage candidate) {
        assertThat(expected.address()).isEqualTo(candidate.address());
        assertThat(expected.flags()).isEqualTo(candidate.flags());
        assertThat(expected.length()).isEqualTo(candidate.length());
        assertThat(expected.buffer()).containsExactly(candidate.buffer());
    }
}

