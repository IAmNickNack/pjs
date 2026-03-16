package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * Serial port for MCP23008/MCP23017 SPI device registers.
 */
public class McpSpiRegister implements SerialPort {
    private final Spi delegate;
    private final int register;
    private final int address;

    public McpSpiRegister(Spi delegate, int register, int address) {
        this.delegate = delegate;
        this.register = register;
        this.address = address;
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int len) {
        var bytesOut = new byte[len + 2];
        bytesOut[0] = (byte) (0b0100_0001 | (address << 1));
        bytesOut[1] = (byte) (register);
        System.arraycopy(buffer, 0, bytesOut, 2, len);

        var bytesIn = new byte[len + 2];
        delegate.transfer(bytesOut, bytesIn);
        System.arraycopy(bytesIn, 2, buffer, 0, len);

        return len;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int len) {
        var bytesOut = new byte[len + 2];
        bytesOut[0] = (byte) (0b0100_0000 | (address << 1));
        bytesOut[1] = (byte) register;
        System.arraycopy(buffer, 0, bytesOut, 2, len);

        delegate.transfer(bytesOut, new byte[len + 2]);
    }

    /**
     * Factory to create registers for MCP23008/MCP23017 SPI devices.
     */
    public static class Factory implements Mcp23xxxRegisterFactory {
        private final Spi delegate;
        private final int address;

        /**
         * Construct a factory for the given SPI device.
         * <p>
         * MCP23xxx devices are not required to have an address.
         * </p>
         * @param delegate the SPI delegate.
         */
        public Factory(Spi delegate) {
            this(delegate, 0);
        }

        /**
         * Construct a factory for the SPI device configured with the given address.
         * @param delegate the SPI delegate.
         * @param address the hardware address of the device.
         */
        public Factory(Spi delegate, int address) {
            this.delegate = delegate;
            this.address = address;
        }

        /**
         * Create a serial port for the given register.
         * @param register the register to create a serial port for.
         * @return the serial port.
         */
        @Override
        public SerialPort register(int register) {
            return new McpSpiRegister(delegate, register, address);
        }
    }
}
