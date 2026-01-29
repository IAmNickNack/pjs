package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.model.port.SerialPort;

public class McpSpiTransferRegister implements SerialPort {
    private final SpiTransfer delegate;
    private final int register;
    private final int address;

    public McpSpiTransferRegister(SpiTransfer delegate, int register, int address) {
        this.delegate = delegate;
        this.register = register;
        this.address = address;
    }

    @Override
    public int readBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(
                SpiTransfer.Message.write(new byte[] { (byte) (0b0100_0001 | (address << 1)) }),
                SpiTransfer.Message.write(new byte[] { (byte) register }),
                SpiTransfer.Message.read(buffer)
        );
        return length;
    }

    @Override
    public void writeBytes(byte[] buffer, int offset, int length) {
        delegate.transfer(
                SpiTransfer.Message.write(new byte[] { (byte) (0b0100_0000 | (address << 1)) }),
                SpiTransfer.Message.write(new byte[] { (byte) register }),
                SpiTransfer.Message.write(buffer)
        );
    }

    /**
     * Factory to create registers for MCP23008/MCP23017 SPI devices.
     */
    public static class Factory implements Mcp23xxxRegisterFactory {
        private final SpiTransfer delegate;
        private final int address;

        /**
         * Construct a factory for the given SPI device.
         * @param delegate the SPI transfer delegate.
         */
        public Factory(SpiTransfer delegate) {
            this(delegate, 0);
        }

        /**
         * Construct a factory for the SPI device configured with the given address.
         * @param delegate the SPI transfer delegate.
         * @param address the hardware address of the device.
         */
        public Factory(SpiTransfer delegate, int address) {
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
            return new McpSpiTransferRegister(delegate, register, address);
        }
    }
}
