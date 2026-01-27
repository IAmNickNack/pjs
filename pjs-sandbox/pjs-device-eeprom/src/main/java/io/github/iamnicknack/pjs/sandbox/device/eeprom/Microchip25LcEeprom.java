package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.model.SerialReadOperation;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;
import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.port.Port;
import io.github.iamnicknack.pjs.model.port.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Microchip25LcEeprom implements Eeprom {

    private final Logger logger = LoggerFactory.getLogger(Microchip25LcEeprom.class);

    private final SpiTransfer delegate;
    private final Pin writeEnableLatch;
    private final Port<Integer> readStatusRegister;
    private final Port<Integer> writeStatusRegister;

    private final Pin holdPin;

    private final PageFunction pageFunction;

    /**
     * Register enum for Microchip 25LC EEPROM commands
     */
    public enum Register {
        /**
         * Read data from memory array
         */
        READ(0b0000_0011),
        /**
         * Write data to memory array
         */
        WRITE(0b0000_0010),
        /**
         * Set the write enable latch
         */
        WREN(0b0000_0110),
        /**
         * Reset the write enable latch
         */
        WRDI(0b0000_0100),
        /**
         * Read STATUS register
         */
        RDSR(0b0000_0101),
        /**
         * Write STATUS register
         */
        WRSR(0b0000_0001);

        public final int value;

        Register(int value) {
            this.value = value;
        }

        public static Register valueOf(int value) {
            return switch (value) {
                case 0b0000_0011 -> READ;
                case 0b0000_0010 -> WRITE;
                case 0b0000_0110 -> WREN;
                case 0b0000_0100 -> WRDI;
                case 0b0000_0101 -> RDSR;
                case 0b0000_0001 -> WRSR;
                default -> throw new IllegalArgumentException("Unknown register value: " + value);
            };
        }
    }

    /**
     * Status flags for the EEPROM
     */
    public enum StatusFlags {
        /**
         * Write in progress
         */
        WIP(1),
        /**
         * Write enable latch is set
         */
        WEL(1 << 1),
        /**
         * Block protection bit 0
         */
        BP0(1 << 2),
        /**
         * Block protection bit 1
         */
        BP1(1 << 3),
        /**
         * All block protection bits
         */
        BP(3 << 2);

        public final int mask;

        StatusFlags(int mask) {
            this.mask = mask;
        }

        public static StatusFlags valueOf(int mask) {
            return switch (mask) {
                case 1 -> WIP;
                case 1 << 1 -> WEL;
                case 1 << 2 -> BP0;
                case 1 << 3 -> BP1;
                default -> throw new IllegalArgumentException("Unknown status flag mask: " + mask);
            };
        }

        public static String toString(int mask) {
            StringBuilder sb = new StringBuilder();

            for (StatusFlags flag : StatusFlags.values()) {
                if ((mask & flag.mask) != 0) {
                    if (!sb.isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(flag.name());
                }
            }

            if (sb.isEmpty()) {
                return "None";
            }

            return sb.toString();
        }
    }

    /**
     * Block protection modes for the EEPROM
     */
    public enum BlockProtection {
        /**
         * Block protection is disabled
         */
        NONE(0),
        /**
         * Block protection is enabled for the upper quarter of the EEPROM
         */
        UPPER_Q(1 << 2),
        /**
         * Block protection is enabled for the upper half of the EEPROM
         */
        UPPER_H(1 << 3),
        /**
         * Block protection is enabled for the entire EEPROM
         */
        ALL(3 << 2);

        public final int mask;

        BlockProtection(int mask) {
            this.mask = mask;
        }

        public static BlockProtection valueOf(int mask) {
            return switch (mask & ALL.mask) {
                case 1 << 2 -> UPPER_Q;
                case 1 << 3 -> UPPER_H;
                case 3 << 2 -> ALL;
                default -> NONE;
            };
        }
    }

    public Microchip25LcEeprom(SpiTransfer delegate, Pin holdPin) {
        this(delegate, holdPin, new PageFunction.DefaultPageFunction(32));
    }

    public Microchip25LcEeprom(SpiTransfer delegate, Pin holdPin, PageFunction pageFunction) {
        this.delegate = delegate;
        this.writeEnableLatch = delegate.pin(Register.WRDI.value, Register.WREN.value);
        this.readStatusRegister = new DeviceRegister(this.delegate, Register.RDSR).input();
        this.writeStatusRegister = new DeviceRegister(this.delegate, Register.WRSR).output();

        this.holdPin = holdPin;
        this.pageFunction = pageFunction;

        this.writeEnableLatch.high();
        this.holdPin.low();
    }

    /**
     * Write data to memory beginning at the selected address.
     * @param address The address to write to.
     * @param data The data to write.
     */
    @Override
    public void write(int address, byte[] data) {
        writeRegister(address).writeBytes(data, 0, data.length);
    }

    /**
     * Fill the specified buffer with data read from memory beginning at the selected address.
     * @param address The address to start reading from.
     * @param length The number of bytes to read.
     * @return The data read.
     */
    @Override
    public byte[] read(int address, int length) {
        return readRegister(address).readBytes(length);
    }

    /**
     * Set the block protection mode for the EEPROM.
     * @param protection The block protection mode to set.
     */
    public void setBlockProtection(BlockProtection protection) {
        holdPin.high();
        writeEnableLatch.low();
        writeStatusRegister.write(protection.mask);
        writeEnableLatch.high();
        holdPin.low();
    }

    /**
     * Get the current block protection mode for the EEPROM.
     * @return The current block protection mode.
     */
    public BlockProtection getBlockProtection() {
        holdPin.high();
        var value =  BlockProtection.valueOf(readStatusRegister.read());
        holdPin.low();
        return value;
    }

    /**
     * Create a serial write operation for the specified address.
     * @param address The base address for the write operation.
     * @return The serial write operation.
     */
    private SerialWriteOperation writeRegister(int address) {
        return (buffer, offset, length) -> {
            byte[] data = (length < buffer.length)
                    ? Arrays.copyOfRange(buffer, offset, offset + length)
                    : buffer;

            var pages = pageFunction.pages(address, data);

            if (pages.hasNext()) {
                holdPin.high();
                while (pages.hasNext()) {
                    var page = pages.next();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Writing to page at address 0x{}", String.format("%04X", page.address()));
                    }

                    var delegate = new DataRegister(this.delegate, Register.WRITE, page.address());

                    writeEnableLatch.low();
                    delegate.writeBytes(page.data(), 0, page.data().length);
                    for (int i = 0; i < 100; i++) {
                        if ((readStatusRegister.read() & StatusFlags.WIP.mask) == 0) {
                            break;
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Waiting for write to complete ({})", i);
                        }
                    }

                    writeEnableLatch.high();
                }
                holdPin.low();
            }
        };
    }

    /**
     * Create a serial read operation for the specified address.
     * @param address The base address for the read operation.
     * @return The serial read operation.
     */
    private SerialReadOperation readRegister(int address) {
        var delegate = new DataRegister(this.delegate, Register.READ, address);
        return (buffer, offset,len) -> {
            holdPin.high();
            var result = delegate.readBytes(buffer, offset, len);
            holdPin.low();
            return result;
        };
    }

    /**
     * A {@link SerialPort} that reads and writes to a specific device register.
     */
    private static class DeviceRegister implements SerialPort {

        private final SpiTransfer delegate;
        private final Register register;

        public DeviceRegister(SpiTransfer delegate, Register register) {
            this.delegate = delegate;
            this.register = register;
        }

        @Override
        public int readBytes(byte[] buffer, int offset, int length) {
            var messages = new SpiTransfer.Message[] {
                    SpiTransfer.Message.write(new byte[] { (byte) register.value }),
                    SpiTransfer.Message.read(buffer, offset, length)
            };
            return delegate.transfer(messages);
        }

        @Override
        public void writeBytes(byte[] buffer, int offset, int length) {
            var messages = new SpiTransfer.Message[] {
                    SpiTransfer.Message.write(new byte[] { (byte) register.value }),
                    SpiTransfer.Message.write(buffer, offset, length)
            };
            delegate.transfer(messages);
        }
    }

    /**
     * A {@link SerialPort} that reads and writes to a specific memory address.
     */
    private static class DataRegister implements SerialPort {
        private final SpiTransfer delegate;
        private final Register register;
        private final int address;

        public DataRegister(SpiTransfer delegate, Register register, int address) {
            this.delegate = delegate;
            this.register = register;
            this.address = address;
        }

        @Override
        public int readBytes(byte[] buffer, int offset, int length) {
            var messages = new SpiTransfer.Message[] {
                    SpiTransfer.Message.write(new byte[] { (byte) register.value }),
                    SpiTransfer.Message.write(new byte[] { (byte) (address >> 8), (byte) address }),
                    SpiTransfer.Message.read(buffer, offset, length)
            };
            return delegate.transfer(messages);
        }

        @Override
        public void writeBytes(byte[] buffer, int offset, int length) {
            var messages = new SpiTransfer.Message[] {
                    SpiTransfer.Message.write(new byte[] { (byte) register.value }),
                    SpiTransfer.Message.write(new byte[] { (byte) (address >> 8), (byte) address }),
                    SpiTransfer.Message.write(buffer, offset, length)
            };
            delegate.transfer(messages);
        }
    }
}
