package io.github.iamnicknack.pjs.model.port;

import io.github.iamnicknack.pjs.model.FakePort;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IntegerPortTest {

    @Test
    void canReadWriteIntegerPort() {
        var port = new FakePort(42);
        var integerPort = new IntegerPort(port);

        assertEquals(42, integerPort.read());
        integerPort.write(100);
        assertEquals(100, port.read());
    }

    @TestFactory
    Stream<DynamicTest> canReadMaskIntegerPort() {
        return IntStream.range(0, 256)
                .mapToObj(i -> DynamicTest.dynamicTest(String.format("mask %02x", i), () -> {
                    var port = new FakePort(0xff);
                    var masked = new IntegerPort(port).masked(i);
                    assertEquals(i, masked.read());
                }));
    }

    @TestFactory
    Stream<DynamicTest> canWriteMaskIntegerPort() {
        return IntStream.range(0, 256)
                .mapToObj(i -> DynamicTest.dynamicTest(String.format("mask %02x", i), () -> {
                    var port = new FakePort(0);
                    var masked = new IntegerPort(port).masked(i);
                    masked.write(0xff);
                    assertEquals(i, port.read());
                }));
    }

    @Test
    void canCreateMappedIntegerPort() {
        var port = new FakePort(42);
        var stringPort = port.mapped(Object::toString, Integer::parseInt);
        var mapped = new IntegerPort(stringPort, Integer::parseInt, Object::toString);

        assertEquals(42, mapped.read());
        mapped.write(100);
        assertEquals(100, port.read());
    }
}
