package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CSerialPort;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver;

import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;

public class DefaultSh1106Driver implements Sh1106Driver {

    /**
     * The {@link SerialWriteOperation} used to write commands to the OLED display.
     */
    private final SerialWriteOperation commandWriteOperation;

    /**
     * The {@link SerialWriteOperation} used to write data to the OLED display.
     */
    private final SerialWriteOperation displayWriteOperation;

    /**
     * The I2C delegate to use for communication.
     */
    private final I2C i2c;

    /**
     * The I2C address of the OLED display.
     */
    private final int address;

    /**
     * Constructor.
     * @param delegate the I2C delegate to use for communication.
     * @param address the I2C address of the OLED display.
     */
    public DefaultSh1106Driver(I2C delegate, int address) {
        this.i2c = delegate;
        this.address = address;
        var delegatePort = new I2CSerialPort(address, delegate);
        this.commandWriteOperation = new DeviceRegisterWriteOperation(0, delegatePort);
        this.displayWriteOperation = new DeviceRegisterWriteOperation(0x40, delegatePort);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void command(CommandSequence commandSequence) {
        commandSequence.writeTo(commandWriteOperation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(byte[] data, int offset, int length) {
        displayWriteOperation.writeBytes(data, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void display(int page, int column, byte[] data, int offset, int length) {
        var commandMessageWriter = new MessageBuildingSerialWriteOperation(address, 0);
        var displayMessageWriter = new MessageBuildingSerialWriteOperation(address, 0x40);

        new CommandSequence()
                .append(Command.PAGE_ADDRESS, page)
                .append(Command.DISPLAY_COLUMN_HIGH, (column + 2) >> 4)
                .append(Command.DISPLAY_COLUMN_LOW, (column + 2) & 0x0f)
                .writeTo(commandMessageWriter);

        displayMessageWriter.writeBytes(data, offset, length);

        var commandMessage = commandMessageWriter.buildMessage();
        var displayMessage = displayMessageWriter.buildMessage();

        i2c.transfer(commandMessage, displayMessage);
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

    /**
     * {@link SerialWriteOperation} that builds an I2C message from the data being written.
     */
    static class MessageBuildingSerialWriteOperation implements SerialWriteOperation {
        private final ByteBuffer buffer = ByteBuffer.allocate(1024);
        private final int address;

        public MessageBuildingSerialWriteOperation(int address, int register) {
            this.address = address;
            buffer.put((byte)register);
        }

        @Override
        public void writeBytes(byte[] buffer, int offset, int length) {
            this.buffer.put(buffer, offset, length);
        }

        public I2C.Message buildMessage() {
            var bytes = new byte[buffer.position()];
            buffer.flip();
            buffer.get(bytes);
            return I2C.Message.write(address, bytes, 0, bytes.length);
        }
    }
}
