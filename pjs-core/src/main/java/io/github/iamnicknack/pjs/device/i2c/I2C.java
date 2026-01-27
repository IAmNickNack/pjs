package io.github.iamnicknack.pjs.device.i2c;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.port.SerialPort;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * I2C contract for devices providing I2C functionality. The implementation follows the RDRW approach used by
 * native Linux I2C drivers.
 * <p>
 * An {@link I2C} instance represents a single I2C bus. This can be used to create a {@link SerialPort} for either:
 * <ul>
 * <li>an addressed I2C device.</li>
 * <li>a register on an addressed I2C device.</li>
 * </ul>
 * </p>
 */
public interface I2C extends Device<I2C> {

    /**
     * A single transfer operation with an i2c start condition per message and a single stop condition.
     * <p>
     * The stop condition is emitted after the final message has been transferred.
     * </p>
     * @param messages the messages to transfer.
     */
    void transfer(Message... messages);

    /**
     * A single I2C message used as part of a transfer operation.
     *
     * @param address the device address.
     * @param data the data to transfer.
     * @param offset the offset into the data array.
     * @param length the length of the data to transfer.
     * @param type the type of the message (read or write).
     */
    record Message(int address, byte[] data, int offset, int length, Type type) {

        /**
         * Create a read message.
         * @param device the device address.
         * @param data the buffer to read into.
         * @return the <code>read</code> message.
         */
        public static Message read(int device, byte[] data, int offset, int length) {
            return new Message(device, data, offset, length, Type.READ);
        }

        /**
         * Create a write message.
         * @param device the device address.
         * @param data the data to write.
         * @return the <code>write</code> message.
         */
        public static Message write(int device, byte[] data, int offset, int length) {
            return new Message(device, data, offset, length, Type.WRITE);
        }

        /**
         * The type of the message. This is used to set the read-/write-bit in the I2C start condition.
         */
        public enum Type {
            READ,
            WRITE
        }

        @Override
        @NonNull
        public String toString() {
            return "Message{" +
                    "address=" + address +
                    ", data=" + Arrays.toString(data) +
                    ", offset=" + offset +
                    ", length=" + length +
                    ", type=" + type +
                    '}';
        }
    }
}
