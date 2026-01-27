package io.github.iamnicknack.pjs.ffm.device.context.i2c;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * <code>
 * struct i2c_rdwr_ioctl_data {
 *      struct i2c_msg *msgs;
 *      __u32 nmsgs;
 * };
 * </code>
 * <p>
 * I2C ioctl read/write data structure.
 * </p>
 * @param messages the messages to read/write
 * @param messageCount the number of messages to read/write
 * @see I2CMessage
 */
@SerializeUsing(I2CRdwrData.Serializer.class)
@DeserializeUsing(I2CRdwrData.Deserializer.class)
public record I2CRdwrData(
        I2CMessage[] messages,
        int messageCount
) {
    public static final int MAX_MESSAGES = 42;

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("messages"),
            ValueLayout.JAVA_INT.withName("messageCount")
    );

    private static final VarHandle VH_MESSAGES = LAYOUT.varHandle(groupElement("messages"));
    private static final VarHandle VH_MESSAGE_COUNT = LAYOUT.varHandle(groupElement("messageCount"));

    public static class Serializer implements MemorySegmentSerializer<I2CRdwrData> {

        private final SegmentAllocator segmentAllocator;
        private final I2CMessage.Serializer messageSerializer;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
            this.messageSerializer = new I2CMessage.Serializer(segmentAllocator);
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(I2CRdwrData data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);

                var bodySegment = segmentAllocator.allocate(data.messages.length * I2CMessage.LAYOUT.byteSize());
                for (int i = 0; i < data.messages.length; i++) {
                    var messageSlice = bodySegment.asSlice(i * I2CMessage.LAYOUT.byteSize(), I2CMessage.LAYOUT.byteSize());
                    var messageSegment = messageSerializer.serialize(data.messages[i]);
                    messageSlice.copyFrom(messageSegment);
                }

                VH_MESSAGES.set(segment, 0L, bodySegment);
                VH_MESSAGE_COUNT.set(segment, 0L, data.messageCount);

                return segment;
            } catch (Throwable throwable) {
                throw new RuntimeException("Failed to serialize I2CRdwrData", throwable);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<I2CRdwrData> {
        private final I2CMessage.Deserializer messageDeserializer = new I2CMessage.Deserializer();

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public I2CRdwrData deserialize(MemorySegment segment) {
            try {
                var messageCount = (int) VH_MESSAGE_COUNT.get(segment, 0L);

                var bodySegment = ((MemorySegment) VH_MESSAGES.get(segment, 0L))
                        .reinterpret(messageCount * I2CMessage.LAYOUT.byteSize());

                var messages = new I2CMessage[messageCount];
                for (int i = 0; i < messageCount; i++) {
                    var messageSlice = bodySegment.asSlice(i * I2CMessage.LAYOUT.byteSize(), I2CMessage.LAYOUT.byteSize());
                    var messageSegment = messageDeserializer.deserialize(messageSlice);
                    messages[i] = messageSegment;
                }

                return new I2CRdwrData(messages, messageCount);
            } catch (Throwable throwable) {
                throw new RuntimeException("Failed to deserialize I2CRdwrData", throwable);
            }
        }
    }

}
