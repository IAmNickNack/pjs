package io.github.iamnicknack.pjs.ffm.device.context.i2c;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.assertj.core.api.Assertions.assertThat;

class I2CRdwrDataTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void test() {
        var data = new I2CRdwrData(
                new I2CMessage[] {
                        new I2CMessage(1, 2, 3, new byte[] { 1, 2, 3 }),
                        new I2CMessage(1, 2, 3, new byte[] { 4, 5, 6 })
                },
                2
        );
        var segment = mapper.segment(data);
        var deserializedData = mapper.value(segment, I2CRdwrData.class);

        assertThat(deserializedData.messageCount()).isEqualTo(2);
        for (int i = 0; i < 2; i++) {
            I2CMessageTest.assertEqual(data.messages()[i], deserializedData.messages()[i]);
        }
    }

}