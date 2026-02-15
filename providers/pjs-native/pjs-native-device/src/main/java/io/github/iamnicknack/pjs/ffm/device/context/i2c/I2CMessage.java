package io.github.iamnicknack.pjs.ffm.device.context.i2c;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/**
 * <code>
 * struct i2c_msg {
 *     __u16 addr;
 *     __u16 flags;
 *     __u16 len;
 *     __u8 *buf;
 * };
 * </code>
 * @param address the address of the I2C device
 * @param flags the flags for the message (see I2C_M_*)
 * @param length the length of the buffer
 * @param buffer the data buffer
 * @see I2CRdwrData
 */
@SerializeUsing(I2CMessage.Serializer.class)
@DeserializeUsing(I2CMessage.Deserializer.class)
public record I2CMessage(
        int address,
        int flags,
        int length,
        byte[] buffer
) {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_SHORT.withName("address"),
            ValueLayout.JAVA_SHORT.withName("flags"),
            ValueLayout.JAVA_SHORT.withName("length"),
            MemoryLayout.paddingLayout(2),
            ValueLayout.ADDRESS.withName("buffer")
    );

    private static final VarHandle VH_ADDRESS = LAYOUT.varHandle(groupElement("address"));
    private static final VarHandle VH_FLAGS = LAYOUT.varHandle(groupElement("flags"));
    private static final VarHandle VH_LENGTH = LAYOUT.varHandle(groupElement("length"));
    private static final VarHandle VH_BUFFER = LAYOUT.varHandle(groupElement("buffer"));

    @Override
    public String toString() {
        return "I2CMessage{" +
                "address=" + address +
                ", flags=" + flags +
                ", length=" + length +
                ", buffer=" + Arrays.toString(buffer) +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof I2CMessage message
                && address == message.address
                && flags == message.flags
                && length == message.length
                && Arrays.equals(buffer, message.buffer);
    }

    public static class Serializer implements MemorySegmentSerializer<I2CMessage> {
        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(I2CMessage data) {
            try {
                var segment = segmentAllocator.allocate(LAYOUT);
                VH_ADDRESS.set(segment, 0L, (short)data.address);
                VH_FLAGS.set(segment, 0L, (short)data.flags);
                VH_LENGTH.set(segment, 0L, (short)data.length);

                var bufferSegment = segmentAllocator.allocate(data.length);
                bufferSegment.asByteBuffer().put(data.buffer);
                VH_BUFFER.set(segment, 0L, bufferSegment);

                return segment;
            } catch (Throwable e) {
                throw new RuntimeException("Failed to serialize I2CMessage", e);
            }
        }
    }

    public static class Deserializer implements MemorySegmentDeserializer<I2CMessage> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public I2CMessage deserialize(MemorySegment segment) {
            try {
                var address = (int) VH_ADDRESS.get(segment, 0L);
                var flags = (int) VH_FLAGS.get(segment, 0L);
                var length = (int) VH_LENGTH.get(segment, 0L);

                var bufferSegment = (MemorySegment)VH_BUFFER.get(segment, 0L);
                var buffer = new byte[length];
                bufferSegment.reinterpret(length).asByteBuffer().get(buffer, 0, length);

                return new I2CMessage(address, flags, length, buffer);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to deserialize I2CMessage", e);
            }
        }
    }
}
