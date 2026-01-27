package io.github.iamnicknack.pjs.sandbox.device.mcp;

import io.github.iamnicknack.pjs.sandbox.device.mcp.register.InMemoryRegister;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Mcp23x08Test {

    @Test
    void test() {
        var registerData = new byte[11];
        var registerFactory = new InMemoryRegister.Factory(registerData);
        var device = new Mcp23x08(registerFactory);

        device.iodir.write(0xff);
        assertEquals((byte)0xff, registerData[0]);
    }
}