package io.github.iamnicknack.pjs.model.port;

import io.github.iamnicknack.pjs.model.FakePort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortTest {

    @Test
    void canCreateReadonlyPort() {
        var port = new FakePort(42).input();
        assertThrows(UnsupportedOperationException.class, () -> port.write(1));
        assertEquals(42, port.read());
    }

    @Test
    void canCreateOutputPort() {
        var port = new FakePort(42);
        var writeOnly = port.output();
        assertThrows(UnsupportedOperationException.class, writeOnly::read);

        writeOnly.write(1);
        assertEquals(1, port.value);
    }

    @Test
    void canCreateMappedPort() {
        var port = new FakePort(42);
        var typed = port.mapped(Object::toString, Integer::parseInt);

        assertEquals("42", typed.read());
        typed.write("100");
        assertEquals(100, port.value);
    }
}
