package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CSerialPort;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;

import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Sh1106Driver {

    /**
     * Commands supported by the SH1106 OLED display.
     */
    public enum Command {
        DISPLAY_COLUMN_LOW(        0b0000_0000, 0b0000_1111),
        DISPLAY_COLUMN_HIGH(       0b0001_0000, 0b0000_1111),
        PUMP_VOLTAGE(              0b0011_0000, 0b0000_0011),
        DISPLAY_START_LINE(        0b0100_0000, 0b0011_1111),
        CONTRAST_MODE(             0b1000_0001, 0xff), // 8-bit value, double byte operation
        SEGMENT_REMAP(             0b1010_0000, 0b0000_0001),
        ENTIRE_DISPLAY_ON_OFF(     0b1010_0100, 0b0000_0001),
        REVERSE_DISPLAY(           0b1010_0110, 0b0000_0001),
        MULTIPLEX_RATIO(           0b1010_1000, 0xff), // 6-bit value, double byte operation
        DC_DC_CONVERTER(           0b1010_1101, 0xff), // 2-bit value, double byte operation
        DC_DC_ON_OFF(              0b1000_1010, 0b0000_0001),
        DISPLAY_ON_OFF(            0b1010_1110, 0b0000_0001),
        PAGE_ADDRESS(              0b1011_0000, 0b0000_1111),
        OUTPUT_SCAN_DIRECTION(     0b1100_0000, 0b0000_1111), // command is bit 3
        DISPLAY_OFFSET(            0b1101_0011, 0xff), // 8-bit value, double byte operation
        DISPLAY_CLOCK_DIVIDE_RATIO(0b1101_0101, 0xff), // 8-bit value, double byte operation
        CHARGE_PERIOD(             0b1101_1001, 0xff), // 8-bit value, double byte operation
        COMMON_PADS_CONFIG(        0b1101_1010, 0xff), // 8-bit value, double byte operation
        VCOM_DESELECT_LEVEL(       0b1101_1011, 0xff), // 8-bit value, double byte operation
        READ_MODIFY_WRITE_START(   0b1110_0000, 0),
        READ_MODIFY_WRITE_END(     0b1110_1110, 0),
        NOP(                       0b1110_0011, 0),
        READ_STATUS(               0, 0), // ???
        ;

        /**
         * The opcode of the command.
         */
        public final int opcode;
        /**
         * The mask for the operand, used to ensure that only valid bits are used and to mask the opcode.
         */
        public final int operandMask;
        /**
         * The operation to append the command to the buffer.
         */
        public final AppendOperation appendOperation;

        /**
         * Constructor.
         * @param opcode the opcode of the command.
         * @param operandMask the mask for the operand.
         */
        Command(int opcode, int operandMask) {
            this.opcode = opcode;
            this.operandMask = operandMask;
            this.appendOperation = (operandMask == 0xff)
                    ? this::appendDoubleByteOperation
                    : this::appendSingleByteOperation;
        }

        /**
         * Append a double-byte command to the buffer.
         * @param appendOperation the operation to append the command to the buffer.
         * @param operand the operand of the command.
         */
        private void appendDoubleByteOperation(Consumer<Byte> appendOperation, int operand) {
            appendOperation.accept((byte)(opcode));
            appendOperation.accept((byte)(operand));
        }

        /**
         * Append a single-byte command to the buffer.
         * @param appendOperation the operation to append the command to the buffer.
         * @param operand the operand of the command.
         */
        private void appendSingleByteOperation(Consumer<Byte> appendOperation, int operand) {
            appendOperation.accept((byte)(opcode | (operand & operandMask)));
        }

        /**
         * The operation used by the command to append itself to a buffer.
         */
        @FunctionalInterface
        public interface AppendOperation {
            void append(Consumer<Byte> appendOperation, int operand);
        }
    }

    /**
     * Known-acceptable command sequence for initialisation.
     */
    public static final CommandSequence DEFAULT_STARTUP_SEQUENCE = new CommandSequence()
            .append(Command.DISPLAY_ON_OFF, 0x00)
            .append(Command.DISPLAY_CLOCK_DIVIDE_RATIO, 0x80)
            .append(Command.MULTIPLEX_RATIO, 0x3f)
            .append(Command.DISPLAY_OFFSET, 0x00)
            .append(Command.DISPLAY_START_LINE, 0x00)
            .append(Command.DC_DC_CONVERTER, 0x8b)
            .append(Command.SEGMENT_REMAP, 0x01)
            .append(Command.OUTPUT_SCAN_DIRECTION, 0x08)
            .append(Command.COMMON_PADS_CONFIG, 0x12)
            .append(Command.CONTRAST_MODE, 0xff)
            .append(Command.CHARGE_PERIOD, 0x1f)
            .append(Command.VCOM_DESELECT_LEVEL, 0x40)
            .append(Command.PUMP_VOLTAGE, 0x03)
            .append(Command.REVERSE_DISPLAY, 0x00)
            .append(Command.ENTIRE_DISPLAY_ON_OFF, 0x00);

    /**
     * The {@link SerialWriteOperation} used to write commands to the OLED display.
     */
    private final SerialWriteOperation commandWriteOperation;

    /**
     * The {@link SerialWriteOperation} used to write data to the OLED display.
     */
    private final SerialWriteOperation displayWriteOperation;

    /**
     * Constructor.
     * @param delegate the I2C delegate to use for communication.
     * @param address the I2C address of the OLED display.
     */
    public Sh1106Driver(I2C delegate, int address) {
        var delegatePort = new I2CSerialPort(address, delegate);
        this.commandWriteOperation = new DeviceRegisterWriteOperation(0, delegatePort);
        this.displayWriteOperation = new DeviceRegisterWriteOperation(0x40, delegatePort);
    }

    /**
     * @return the {@link SerialWriteOperation} used to write commands to the OLED display.
     */
    public SerialWriteOperation getCommandWriteOperation() {
        return commandWriteOperation;
    }

    /**
     * @return Get the {@link SerialWriteOperation} used to write data to the OLED display.
     */
    public SerialWriteOperation getDisplayWriteOperation() {
        return displayWriteOperation;
    }

    /**
     * Send a command sequence to the OLED display.
     * @param commandSequence the command sequence to send.
     */
    public void command(CommandSequence commandSequence) {
        commandSequence.writeTo(commandWriteOperation);
    }

    /**
     * Send a single command to the OLED display.
     * @param command the command to send.
     * @param operand the operand of the command.
     */
    public void command(Command command, int operand) {
        new CommandSequence()
                .append(command, operand).writeTo(commandWriteOperation);
    }

    /**
     * Send display data to the device
     * @param page the page to write to
     * @param column the column to start writing at
     * @param data the data to write
     * @param offset the offset into the data array to start writing from
     * @param length the number of bytes to write
     */
    public void display(int page, int column, byte[] data, int offset, int length) {
        command(new CommandSequence()
                .append(Command.PAGE_ADDRESS, page)
                .append(Command.DISPLAY_COLUMN_HIGH, (column + 2) >> 4)
                .append(Command.DISPLAY_COLUMN_LOW, (column + 2) & 0x0f)
        );

        displayWriteOperation.writeBytes(data, offset, length);
    }

    /**
     * Send display data to the device
     * @param data the data to write
     * @param offset the offset into the data array to start writing from
     * @param length the number of bytes to write
     */
    public void display(byte[] data, int offset, int length) {
        displayWriteOperation.writeBytes(data, offset, length);
    }


    /**
     * Builder for a sequence of commands to be sent to the OLED display.
     */
    public static class CommandSequence {
        private final ByteBuffer buffer = ByteBuffer
                .allocate(128);

        public CommandSequence append(Command command, int value) {
            command.appendOperation.append(buffer::put, value);
            return this;
        }
        public CommandSequence append(Command command) {
            command.appendOperation.append(buffer::put, 0);
            return this;
        }

        public void writeTo(SerialWriteOperation writeOperation) {
            buffer.flip();
            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            writeOperation.writeBytes(bytes, 0, bytes.length);
        }
    }

    /**
     * {@link SerialWriteOperation} that prepends the device register to the data being written.
     */
    static class DeviceRegisterWriteOperation implements SerialWriteOperation {
        private final int deviceRegister;
        private final SerialWriteOperation delegate;

        public DeviceRegisterWriteOperation(int deviceRegister, SerialWriteOperation delegate) {
            this.deviceRegister = deviceRegister;
            this.delegate = delegate;
        }

        @Override
        public void writeBytes(byte[] buffer, int offset, int length) {
            try(var outputStream = new BufferedOutputStream(delegate.getOutputStream(), length + 1)) {
                outputStream.write(deviceRegister);
                outputStream.write(buffer, offset, length);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
