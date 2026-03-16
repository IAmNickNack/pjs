package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WriteThroughRegisterTest {

    @Test
    void proxiesValue() {
        var delegateBuffer = new byte[11];
        var delegateRegisterFactory = new InMemoryRegister.Factory(delegateBuffer);

        var cacheBuffer = new byte[11];
        var cacheRegisterFactory = new InMemoryRegister.Factory(cacheBuffer);

        var proxyRegisterFactory = new WriteThroughRegister.Factory(delegateRegisterFactory, cacheRegisterFactory);
        var proxyRegister = proxyRegisterFactory.register(0x09);

        proxyRegister.write(1);
        assertEquals(1, delegateBuffer[9]);
        assertEquals(1, cacheBuffer[9]);
    }

    @Test
    void proxiesAllValues() {
        var delegateBuffer = new byte[11];
        var delegateRegisterFactory = new InMemoryRegister.Factory(delegateBuffer);

        var cacheBuffer = new byte[11];
        var cacheRegisterFactory = new InMemoryRegister.Factory(cacheBuffer);

        var proxyRegisterFactory = new WriteThroughRegister.Factory(delegateRegisterFactory, cacheRegisterFactory);
        var proxyRegister = proxyRegisterFactory.register(0);

        var bytes = new byte[11];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        proxyRegister.writeBytes(bytes);

        for (int i = 0; i < bytes.length; i++) {
            assertEquals(i, delegateBuffer[i]);
            assertEquals(i, cacheBuffer[i]);
        }
    }

}