package io.github.iamnicknack.pjs.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialWriteOperationTest {

    @Test
    void canWriteValue() {
        var operation = new FakeSerialPort();
        operation.writeByte((byte)1);
        assertEquals(1, operation.getBuffer().get(0));
    }

    @Test
    void canWriteBuffer() {
        var operation = new FakeSerialPort();
        operation.writeBytes(new byte[] { 1, 2, 3 });

        var buffer = new byte[3];
        System.arraycopy(operation.getBuffer().array(), 0, buffer, 0, 3);

        Assertions.assertThat(buffer)
                .containsExactly(1, 2, 3);
    }

    @Test
    void canWriteToStream() throws IOException {
        var operation = new FakeSerialPort();
        try(var stream = operation.getOutputStream()) {
            stream.write(0);
            stream.write(new byte[] { 1, 2, 3 });
            stream.write(new byte[] { 3, 4, 5, 6}, 1, 3);
        }
        var buffer = new byte[7];
        System.arraycopy(operation.getBuffer().array(), 0, buffer, 0, 7);

        Assertions.assertThat(buffer)
                .containsExactly(0, 1, 2, 3, 4, 5, 6);
    }
}