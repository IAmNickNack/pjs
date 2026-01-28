package io.github.iamnicknack.pjs.device.spi;

import io.github.iamnicknack.pjs.model.port.SerialPort;

import java.util.Arrays;

/**
 * An SPI transfer operation allows more complex SPI operations to be expressed as a sequence of messages,
 * where each message serves a specific purpose.
 * <p>
 * For example, reading from and writing to a register on a device requires several distinct pieces of information
 * to be provided:
 * <ul>
 *     <li>the register address</li>
 *     <li>whether this is a read or write operation</li>
 *     <li>the payload to read or write</li>
 * </ul>
 * Breaking these out into individual segments is a benefit to the developer, as it allows each segment of the
 * transaction to be clearly defined in code.
 * <p>
 * Practically, such an approach may also be required by the peripheral device.
 * <p>
 * Isolating this ambiguity allows the API not to be opinionated about "what is best" and leaves the choice of
 * implementation to the developer, who is best placed to understand the application use-case.
 */
@FunctionalInterface
public interface SpiTransfer extends SerialPort {

    /**
     * Perform a transfer
     * @param messages the messages to transfer
     * @return the number of bytes transferred
     */
    int transfer(Message... messages);

    /**
     * {@inheritDoc}
     */
    @Override
    default int readBytes(byte[] buffer, int offset, int length) {
        return transfer(Message.read(buffer, offset, length));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void writeBytes(byte[] buffer, int offset, int length) {
        transfer(Message.write(buffer, offset, length));
    }

    /**
     * The message structure used for SPI transfer operations
     */
    interface Message {
        /**
         * the write buffer
         */
        byte[] write();

        /**
         * the offset in the write buffer at which to insert data
         */
        int writeOffset();

        /**
         * the read buffer
         */
        byte[] read();

        /**
         * the offset in the read buffer at which to start reading data
         */
        int readOffset();

        /**
         * the number of bytes to transfer
         */
        int length();

        /**
         * the delay between messages
         */
        int delayUs();

        /**
         * whether to toggle the chip select after this message
         */
        boolean csChange();

        /**
         * Create a write-only message.
         * @param bytes the bytes to write.
         * @return the message.
         */
        static Message write(byte[] bytes) {
            return new DefaultMessage(bytes, 0, new byte[bytes.length], 0, bytes.length);
        }

        /**
         * Create a write-only message.
         * @param bytes the bytes to write.
         * @param offset the offset in the buffer to start writing from.
         * @param length the number of bytes to write.
         * @return the message.
         */
        static Message write(byte[] bytes, int offset, int length) {
            return new DefaultMessage(bytes, offset, new byte[length], 0, length);
        }

        /**
         * Create a read-only message.
         * @param bytes the buffer to read into.
         * @return the message.
         */
        static Message read(byte[] bytes) {
            return new DefaultMessage(new byte[bytes.length], 0, bytes, 0, bytes.length);
        }

        /**
         * Create a read-only message.
         * @param bytes the buffer to read into.
         * @param offset the offset in the buffer to start reading into.
         * @param length the number of bytes to read.
         * @return the message.
         */
        static Message read(byte[] bytes, int offset, int length) {
            return new DefaultMessage(new byte[length], 0, bytes, offset, length);
        }

        /**
         * Get a slice of the write buffer for this message.
         * @return the write buffer slice.
         */
        default byte[] sliceWrite() {
            if (writeOffset() == 0 && write().length == length()) {
                return write();
            } else {
                return Arrays.copyOfRange(write(), writeOffset(), writeOffset() + length());
            }
        }
    }


    /**
     * Data structure to represent a single portion of an SPI transfer.
     * @param write the data to write.
     * @param writeOffset the offset in the write buffer.
     * @param read the data to read.
     * @param readOffset the offset in the read buffer.
     * @param length the number of bytes to transfer.
     * @param delayUs delay between words within one transfer, in microseconds.
     * @param csChange True to deselect the device before starting the next transfer message.
     */
    record DefaultMessage(
            byte[] write,
            int writeOffset,
            byte[] read,
            int readOffset,
            int length,
            int delayUs,
            boolean csChange
    ) implements Message {
        /**
         * Alternative constructor with default delay (0 us) and csChange (false).
         */
        public DefaultMessage(
                byte[] write,
                int writeOffset,
                byte[] read,
                int readOffset,
                int length
        ) {
            this(write, writeOffset, read, readOffset, length, 0, false);
        }
    }
}
