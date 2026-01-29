package io.github.iamnicknack.pjs.sandbox.device.mcp.register;

import io.github.iamnicknack.pjs.model.port.SerialPort;

/**
 * Factory object for creating {@link SerialPort} instances for MCP23xxx-like registers.
 */
@FunctionalInterface
public interface Mcp23xxxRegisterFactory {

    /**
     * Create a serial port for the given register.
     * @param register the register to create a serial port for.
     * @return the serial port.
     */
    SerialPort register(int register);
}
