package io.github.iamnicknack.pjs.pi4j;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

/**
 * {@link I2C} implementation backed by Pi4J.
 */
public class Pi4jI2C implements I2C {

    /**
     * The PJs configuration for this device.
     */
    private final I2CConfig config;

    /**
     * The PJs I2C provider owning and used by this device.
     */
    private final Pi4jI2CProvider provider;

    /**
     * Constructor.
     * @param config the PJs configuration for this device.
     * @param provider the PJs I2C provider owning and used by this device.
     */
    public Pi4jI2C(I2CConfig config, Pi4jI2CProvider provider) {
        this.config = config;
        this.provider = provider;
    }

    /**
     * Handle how Pi4J expects messages to be transferred.
     * <p>
     * For general write operations, the Pi4J FFM API expects a single message.
     * <p>
     * Pi4J also supports register-like operations with 2 messages, one writing the register address
     * and the other containing the buffer to read or write.
     * <p>
     * Pi4J does not currently implement multi-message transfers of 3 or more messages.
     * @param messages the messages to transfer.
     */
    @Override
    public void transfer(Message... messages) {
        if (messages.length == 1) {
            transferSingleMessage(messages[0]);
        } else if (messages.length == 2) {
            transferRegisterMessage(messages[0], messages[1]);
        } else {
            throw new IllegalArgumentException("Only single and double messages are supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceConfig<I2C> getConfig() {
        return this.config;
    }

    private void transferSingleMessage(Message message) {
        var device = provider.deviceForAddress(message.address(), this.config);
        if (message.type() == Message.Type.READ) {
            device.read(message.data(), message.offset(), message.length());
        } else if (message.type() == Message.Type.WRITE) {
            device.write(message.data(), message.offset(), message.length());
        } else {
            throw new IllegalArgumentException("Only READ and WRITE messages are supported");
        }
    }

    private void transferRegisterMessage(Message opMessage, Message dataMessage) {
        var device = provider.deviceForAddress(opMessage.address(), this.config);
        if (opMessage.type() == Message.Type.READ) {
            device.readRegister(opMessage.data()[0], dataMessage.data(), dataMessage.offset(), dataMessage.length());
        } else if (opMessage.type() == Message.Type.WRITE) {
            device.writeRegister(opMessage.data()[0], dataMessage.data(), dataMessage.offset(), dataMessage.length());
        } else {
            throw new IllegalArgumentException("Only READ and WRITE messages are supported");
        }
    }
}
