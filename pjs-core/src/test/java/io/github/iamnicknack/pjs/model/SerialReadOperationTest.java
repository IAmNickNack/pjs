package io.github.iamnicknack.pjs.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialReadOperationTest {


    @Test
    void canReadValue() {
        var data = new byte[] { 1, 2, 3 };
        var operation = new FakeSerialPort(data);

        assertEquals(data[0], operation.readByte());
        assertEquals(data[1], operation.readByte());
        assertEquals(data[2], operation.readByte());
    }

    @Test
    void canReadBuffer() {
        var data = new byte[] { 1, 2, 3 };
        var operation = new FakeSerialPort(data);

        var buffer = new byte[3];
        operation.readBytes(buffer);

        assertThat(buffer)
                .containsExactly(1, 2, 3);
    }

    @Test
    void canReadFromStream() throws IOException {
        var operation = new FakeSerialPort(new byte[] { 0, 1, 2, 3, 4, 5, 6 });
        var data = new byte[7];
        try(var stream = operation.getInputStream()) {
            data[0] = (byte)stream.read();
            var buffer = new byte[6];
            var count = stream.read(buffer);
            System.arraycopy(buffer, 0, data, 1, count);
        }

        assertThat(data)
                .containsExactly(0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    void canReadFromStreamWithOffset() throws IOException {
        var operation = new FakeSerialPort(new byte[] { 0, 1, 2, 3, 4, 5, 6 });
        var data = new byte[7];
        var count = 0;
        try(var stream = operation.getInputStream()) {
            data[0] = (byte)stream.read();
            count = stream.read(data, 1, 6);
        }

        assertEquals(6, count);
        assertThat(data)
                .containsExactly(0, 1, 2, 3, 4, 5, 6);
    }
}