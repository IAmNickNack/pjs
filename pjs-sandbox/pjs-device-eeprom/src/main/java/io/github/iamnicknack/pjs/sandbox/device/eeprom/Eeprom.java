package io.github.iamnicknack.pjs.sandbox.device.eeprom;

/**
 * Represents an interface for an EEPROM device.
 * This interface defines basic operations to interact with EEPROM memory, including reading
 * and writing data.
 */
public interface Eeprom {

    /**
     * Write data to the EEPROM at the specified address.
     * @param address The address to write to.
     * @param data The data to write.
     */
    void write(int address, byte[] data);

    /**
     * Read data from the EEPROM at the specified address.
     * @param address The address to read from.
     * @param length The number of bytes to read.
     * @return The data read.
     */
    byte[] read(int address, int length);
}
