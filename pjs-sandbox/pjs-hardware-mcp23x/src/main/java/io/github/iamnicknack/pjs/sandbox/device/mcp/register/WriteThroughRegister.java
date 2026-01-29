package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.model.port.SerialPort;

public class WriteThroughRegister implements SerialPort {
    private final SerialPort register;
    private final SerialPort cache;

    public WriteThroughRegister(SerialPort register, SerialPort cache) {
        this.register = register;
        this.cache = cache;
    }

    public SerialPort getCache() {
        return cache;
    }

    @Override
    public Integer read() {
        var value = register.read();
        cache.write(value);
        return value;
    }

    @Override
    public void write(Integer value) {
        register.write(value);
        cache.write(value);
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        var result = register.readBytes(buffer, offset, length);
        cache.writeBytes(buffer, offset, result);
        return result;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        register.writeBytes(buffer, offset, length);
        cache.writeBytes(buffer, offset, length);
    }

    public static class Factory implements Mcp23xxxRegisterFactory {
        private final Mcp23xxxRegisterFactory delegateFactory;
        private final Mcp23xxxRegisterFactory cacheFactory;

        public Factory(Mcp23xxxRegisterFactory delegate, Mcp23xxxRegisterFactory cache) {
            this.delegateFactory = delegate;
            this.cacheFactory = cache;
        }

        @Override
        public WriteThroughRegister register(int register) {
            return new WriteThroughRegister(delegateFactory.register(register), cacheFactory.register(register));
        }
    }
}
