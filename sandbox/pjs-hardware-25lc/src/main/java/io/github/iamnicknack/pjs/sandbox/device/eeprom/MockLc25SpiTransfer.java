package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.device.spi.impl.CompositeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of an SPI transfer for a Microchip 25LC series EEPROM.
 */
public class MockLc25SpiTransfer implements SpiTransfer {

    private final int capacity;
    private final int pageSize;
    private final byte[] data;

    private int statusRegisterValue = 0;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MockLc25SpiTransfer(int capacity, int pageSize) {
        this.capacity = capacity;
        this.pageSize = pageSize;

        data = new byte[capacity];
    }

    public byte[] getData() {
        return data;
    }

    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
        var register = Microchip25LcEeprom.Register.valueOf(write[0]);

        switch (register) {
            case READ -> {
                logger.info("Read operation");
                if (length >= 3) {
                    var address = (write[1] & 0xFF) << 8 | (write[2] & 0xFF);

                    for (int i = 0; i < length - 3; i++) {
                        var wrappedAddress = (address + i) % capacity;
                        read[readOffset + i + 3] = data[wrappedAddress];
                    }

                    return length - 3;
                } else {
                    throw new IllegalArgumentException("Invalid read operation");
                }
            }
            case WRITE -> {
                logger.info("Write operation");
                if ((statusRegisterValue & Microchip25LcEeprom.StatusFlags.WEL.mask) == 0) {
                    return length - 3;
                }
                if (length >= 3) {
                    var address = (write[1] & 0xFF) << 8 | (write[2] & 0xFF);
                    var pageAddress = pageAddress(address);
                    var offset = address % pageSize;

                    for (int i = 0; i < length - 3; i++) {
                        var wrappedAddress = (pageAddress + ((offset + i) % pageSize)) % capacity;
                        data[wrappedAddress] = write[writeOffset + i + 3];
                    }

                    return length - 3;
                } else {
                    throw new IllegalArgumentException("Invalid write operation");
                }
            }
            case WREN -> {
                statusRegisterValue |= Microchip25LcEeprom.StatusFlags.WEL.mask;
                logger.info("Write enable: {}", Microchip25LcEeprom.StatusFlags.toString(statusRegisterValue));
                return 1;
            }
            case WRDI -> {
                statusRegisterValue &= ~Microchip25LcEeprom.StatusFlags.WEL.mask;
                logger.info("Write disable: {}", Microchip25LcEeprom.StatusFlags.toString(statusRegisterValue));
                return 1;
            }
            case RDSR -> {
                logger.info("Read status register: {}", Microchip25LcEeprom.StatusFlags.toString(statusRegisterValue));
                return statusRegisterValue;
            }
            case WRSR -> {
                statusRegisterValue = (statusRegisterValue & Microchip25LcEeprom.StatusFlags.BP.mask)
                        | (write[1] & ~Microchip25LcEeprom.StatusFlags.BP.mask);
                logger.info("Write status register: {}", Microchip25LcEeprom.StatusFlags.toString(statusRegisterValue));
                return 1;
            }
        }

        return 0;
    }

    @Override
    public int transfer(Message... messages) {
        var builder = CompositeMessage.builder();
        for (var message : messages) {
            builder.message(message);
        }
        var compositeMessage = builder.build();

        transfer(compositeMessage.write(), 0, compositeMessage.read(), 0, compositeMessage.length());
        compositeMessage.unpack();

        return compositeMessage.length();
    }

    public int pageOffset(int address) {
        return (address % capacity) % pageSize;
    }

    public int pageAddress(int address) {
        return (address % capacity) & -pageSize;
    }


}
