package io.github.iamnicknack.pjs.model.pin;

import io.github.iamnicknack.pjs.model.FakePort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanPinTest {

    @Test
    void canCreateBooleanPinFromPort() {
        var port = new FakePort(1);
        var booleanPort = port.mapped(i -> i != 0, b -> b ? 1 : 0);
        var booleanPin = new BooleanPin(booleanPort);

        assertTrue(booleanPin.read());
        booleanPin.write(false);
        assertEquals(0, port.read());
    }

    @Test
    void canCreateMappedBooleanPin() {
        var port = new FakePort(1);
        var booleanPin = new BooleanPin(port, i -> i != 0, b -> b ? 1 : 0);

        assertTrue(booleanPin.read());
        booleanPin.write(false);
        assertEquals(0, port.read());
    }
}
