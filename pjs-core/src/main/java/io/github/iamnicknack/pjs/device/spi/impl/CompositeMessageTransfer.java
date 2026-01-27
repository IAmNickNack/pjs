package io.github.iamnicknack.pjs.device.spi.impl;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;

/**
 * A {@link SpiTransfer} implementation which can emulate multiple {@link Message}s which may have
 * {@link Message#csChange()} set.
 * <p>
 * The maximum total message length is 4096 bytes.
 * </p>
 */
public class CompositeMessageTransfer implements SpiTransfer {

    public static final int MAX_COMPOSITE_MESSAGE_LENGTH = 4096;

    private final Spi delegate;

    public CompositeMessageTransfer(Spi delegate) {
        this.delegate = delegate;
    }

    @Override
    public int transfer(Message... messages) {
        var length = 0;
        var builder = CompositeMessage.builder();
        for (Message message : messages) {
            length += message.length();
            builder.message(message);
            if (message.csChange()) {
                var compositeMessage = builder.build();
                delegate.transfer(compositeMessage.write(), 0, compositeMessage.read(), 0, compositeMessage.length());
                compositeMessage.unpack();
                builder = CompositeMessage.builder();
            }
        }

        var compositeMessage = builder.build();
        if (compositeMessage.messageCount() > 0) {
            delegate.transfer(compositeMessage.write(), 0, compositeMessage.read(), 0, compositeMessage.length());
            compositeMessage.unpack();
        }

        return length;
    }

}
