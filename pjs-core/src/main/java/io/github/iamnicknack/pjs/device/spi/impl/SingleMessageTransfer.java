package io.github.iamnicknack.pjs.device.spi.impl;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;

import java.util.Arrays;

/**
 * A {@link SpiTransfer} implementation that transfers {@link Message}s one by one.
 */
public class SingleMessageTransfer implements SpiTransfer {

    private final Spi delegate;

    public SingleMessageTransfer(Spi delegate) {
        this.delegate = delegate;
    }

    @Override
    public int transfer(Message... messages) {
        return Arrays.stream(messages)
                .mapToInt(message ->
                        delegate.transfer(
                                message.write(),
                                message.writeOffset(),
                                message.read(),
                                message.readOffset(),
                                message.length()
                        )
                )
                .sum();
    }
}
