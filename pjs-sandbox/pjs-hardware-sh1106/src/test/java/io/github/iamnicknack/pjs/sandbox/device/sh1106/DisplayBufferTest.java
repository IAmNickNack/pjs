package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import org.junit.jupiter.api.Test;

class DisplayBufferTest {

    @Test
    void dirtyBitsAreStoredByReference() {
        var buffer = new DisplayBuffer();

        buffer.setData(0, 0, new byte[1], 0, 1);

        for (int i = 0; i < 16; i++) {
            assert buffer.isDirty(0, i);
        }
        for (int i = 0; i < 16; i++) {
            assert !buffer.isDirty(0, i + 16);
        }
        for (int i = 0; i < 16; i++) {
            assert !buffer.isDirty(1, i);
        }
    }

}