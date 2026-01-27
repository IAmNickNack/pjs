package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CRegister;
import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * Factory to create registers for MCP23008/MCP23017 I2C devices.
 */
public class McpI2CRegisterFactory implements Mcp23xxxRegisterFactory {
    private final I2C delegate;
    private final int address;

    /**
     * Create a serial port for the given register.
     * @param delegate the i2c delegate representing the i2c bus
     * @param address the i2c address of the device.
     */
    public McpI2CRegisterFactory(I2C delegate, int address) {
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
        return new I2CRegister(address, register, delegate);
    }
}
