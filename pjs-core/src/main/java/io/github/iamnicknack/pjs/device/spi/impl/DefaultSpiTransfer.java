package io.github.iamnicknack.pjs.device.spi.impl;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;

/**
 * A default implementation of {@link SpiTransfer}, usable with any {@link Spi} implementation.
 * <p>
 * This {@link SpiTransfer} is orchestration over multiple child implementations.
 * The actual transfer implementation is chosen based on the number of {@link Message}s passed to the transfer method.
 * </p>
 */
public class DefaultSpiTransfer implements SpiTransfer {

    private final SpiTransfer singleMessageTransfer;
    private final SpiTransfer multiMessageTransfer;

    public DefaultSpiTransfer(Spi spi) {
        this.singleMessageTransfer = new SingleMessageTransfer(spi);
        this.multiMessageTransfer = new CompositeMessageTransfer(spi);
    }

    public DefaultSpiTransfer(
            SpiTransfer singleMessageTransfer,
            SpiTransfer multiMessageTransfer
    ) {
        this.singleMessageTransfer = singleMessageTransfer;
        this.multiMessageTransfer = multiMessageTransfer;
    }

    @Override
    public int transfer(Message... messages) {
        return (messages.length == 1)
                ? singleMessageTransfer.transfer(messages)
                : multiMessageTransfer.transfer(messages);
    }

}
