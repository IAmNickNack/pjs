package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.model.port.SerialPort;

public class InMemoryRegister implements SerialPort {

    private final byte[] registerValues;
    private final int register;

    public InMemoryRegister(int register, byte[] registerValues) {
        this.register = register;
        this.registerValues = registerValues;
    }

    @Override
    public void write(Integer value) {
        registerValues[register] = value.byteValue();
    }

    @Override
    public Integer read() {
        return (int)registerValues[register];
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            int index = (register + i) % registerValues.length;
            buffer[offset + i] = registerValues[index];
        }
        return length;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            int index = (register + i) % registerValues.length;
            registerValues[index] = buffer[offset + i];
        }
    }

    public static class Factory implements Mcp23xxxRegisterFactory {
        private final byte[] registerValues;
        public Factory(byte[] registerValues) {
            this.registerValues = registerValues;
        }

        @Override
        public SerialPort register(int register) {
            return new InMemoryRegister(register, registerValues);
        }
    }
}
