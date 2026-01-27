package io.github.iamnicknack.pjs.device.spi.impl;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link SpiTransfer.Message} implementation which can be composed of multiple {@link SpiTransfer.Message}s.
 */
public class CompositeMessage implements SpiTransfer.Message, Iterable<SpiTransfer.Message> {

    private final List<MessagePair> messages;
    private final byte[] writeBuffer;
    private final byte[] readBuffer;
    private final int length;
    private final int delayUs;

    private CompositeMessage(List<MessagePair> messages, byte[] writeBuffer, byte[] readBuffer, int length, int delayUs) {
        this.messages = messages;
        this.writeBuffer = writeBuffer;
        this.readBuffer = readBuffer;
        this.length = length;
        this.delayUs = delayUs;
    }

    public int messageCount() {
        return messages.size();
    }

    @Override
    public byte[] write() {
        return writeBuffer;
    }

    @Override
    public int writeOffset() {
        return 0;
    }

    @Override
    public byte[] read() {
        return readBuffer;
    }

    @Override
    public int readOffset() {
        return 0;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public int delayUs() {
        return delayUs;
    }

    @Override
    public boolean csChange() {
        return true;
    }

    /**
     * Unpack the composite message's read-buffer into the original message buffers.
     */
    public void unpack() {
        messages.forEach(pair ->
                System.arraycopy(
                        pair.composite.read(),
                        pair.composite.readOffset(),
                        pair.original.read(),
                        0,
                        pair.composite.length()
                )
        );
    }

    /**
     * Pack multiple messages into a composite message.
     * @param messages the messages to pack
     * @return the composite message
     */
    public static CompositeMessage pack(SpiTransfer.Message... messages) {
        var builder = CompositeMessage.builder();
        for (var message : messages) {
            builder.message(message);
        }
        return builder.build();
    }

    @Override
    @NonNull
    public Iterator<SpiTransfer.Message> iterator() {
        return messages.stream()
                .map(MessagePair::original)
                .iterator();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<MessagePair> messages = new ArrayList<>();
        private int offset = 0;
        private int delayUs = 0;
        private final byte[] writeBuffer = new byte[CompositeMessageTransfer.MAX_COMPOSITE_MESSAGE_LENGTH];
        private final byte[] readBuffer = new byte[CompositeMessageTransfer.MAX_COMPOSITE_MESSAGE_LENGTH];

        public Builder message(SpiTransfer.Message message) {
            System.arraycopy(message.write(), message.writeOffset(), writeBuffer, offset, message.length());
            delayUs = Math.max(delayUs, message.delayUs());
            var msg = new SpiTransfer.DefaultMessage(
                    writeBuffer, offset, readBuffer, offset, message.length(), message.delayUs(), message.csChange()
            );
            messages.add(new MessagePair(message, msg));
            offset += message.length();
            return this;
        }

        public CompositeMessage build() {
            return new CompositeMessage(messages, writeBuffer, readBuffer, offset, delayUs);
        }
    }

    record MessagePair(
            SpiTransfer.Message original,
            SpiTransfer.Message composite
    ) {
    }
}
