package io.github.iamnicknack.pjs.ffm.device.context.gpio;

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
 * Data for file descriptor event polling
 * @param fd the file descriptor
 * @param events mask indicating events to listen for
 * @param revents returned event mask
 * @see <a href="https://man7.org/linux/man-pages/man2/poll.2.html">man poll</a>
 */
@SerializeUsing(Poll.Serializer.class)
@DeserializeUsing(Poll.Deserializer.class)
public record Poll(
        int fd,
        int events,
        int revents
) {
    public Poll(int fd) {
        this(fd, Flags.POLLIN.value | Flags.POLLERR.value, 0);
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("fd"),
            ValueLayout.JAVA_SHORT.withName("events"),
/*
 * Pi4j FFM has 2 bytes of padding here.
 * Padding doesn't work here, so commenting it out.
 */
//            MemoryLayout.paddingLayout(2),
            ValueLayout.JAVA_SHORT.withName("revents")
    );

    private static final VarHandle VH_FD = LAYOUT.varHandle(groupElement("fd"));
    private static final VarHandle VH_EVENTS = LAYOUT.varHandle(groupElement("events"));
    private static final VarHandle VH_REVENTS = LAYOUT.varHandle(groupElement("revents"));

    public static class Serializer implements MemorySegmentSerializer<Poll> {
        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return  LAYOUT;
        }

        @Override
        public MemorySegment serialize(Poll data) {
            var segment = segmentAllocator.allocate(LAYOUT);

            VH_FD.set(segment, 0, data.fd);
            VH_EVENTS.set(segment, 0, (short)data.events);
            VH_REVENTS.set(segment, 0, (short)data.revents);

            return segment;
        }
    }


    public static class Deserializer implements MemorySegmentDeserializer<Poll> {
        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public Poll deserialize(MemorySegment segment) {
            int fd = (int) VH_FD.get(segment, 0L);
            int events = (int) VH_EVENTS.get(segment, 0L);
            int revents = (int) VH_REVENTS.get(segment, 0L);
            return new Poll(fd, events, revents);
        }
    }

    public enum Flags {
        POLLIN(0x0001),
        POLLERR(0x0008);

        public final int value;

        Flags(int value) {
            this.value = value;
        }
    }
}
