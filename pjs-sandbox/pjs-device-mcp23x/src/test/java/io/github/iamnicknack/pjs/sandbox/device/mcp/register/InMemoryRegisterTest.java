package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryRegisterTest {

    @TestFactory
    Stream<DynamicTest> registerAddressesWrap() {
        var bytesToWrite = new byte[10];
        for (int i = 0; i < bytesToWrite.length; i++) {
            bytesToWrite[i] = (byte) i;
        }

        return IntStream.range(0, 10)
                .mapToObj(registerAddress -> DynamicTest.dynamicTest(String.format("register %d", registerAddress), () -> {
                    var buffer = new byte[10];
                    var factory = new InMemoryRegister.Factory(buffer);
                    var register = factory.register(registerAddress);

                    register.writeBytes(bytesToWrite);

                    for (int i = 0; i < buffer.length; i++) {
                        var wrappedAddress = (registerAddress + i) % buffer.length;
                        assertEquals(bytesToWrite[i], buffer[wrappedAddress]);

                        var checkRegister = factory.register(wrappedAddress);
                        assertEquals(bytesToWrite[i], checkRegister.read());
                    }
                }));
    }
}