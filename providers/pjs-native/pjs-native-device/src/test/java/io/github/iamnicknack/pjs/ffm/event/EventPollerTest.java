package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineEvent;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventPollerTest {

    private final SegmentAllocator segmentAllocator = Arena.ofAuto();

    private final PollingOperations pollingOperations = (poll, _) ->
            new Poll(poll.fd(), Poll.Flags.POLLIN.value | Poll.Flags.POLLERR.value, Poll.Flags.POLLIN.value);

    @TestFactory
    Stream<DynamicTest> canReadEvent() {

        record Expectation(
                String name,
                LineEvent[] events,
                Function<List<PollEvent>, Boolean> predicate
        ) {}

        return Stream.of(
                new Expectation(
                        "Captures event",
                        new LineEvent[] { new LineEvent(1, 2, 3, 4, 5) },
                        pollEvents -> pollEvents.size() == 1
                ),
                new Expectation(
                        "Captures events",
                        new LineEvent[] {
                                new LineEvent(1, 2, 3, 4, 5),
                                new LineEvent(6, 7, 8, 9, 10),
                        },
                        pollEvents -> pollEvents.size() == 2
                ),
                new Expectation(
                        "Stops when timestamp is zero",
                        new LineEvent[] {
                                new LineEvent(1, 2, 3, 4, 5),
                                new LineEvent(0, 7, 8, 9, 10),
                                new LineEvent(6, 7, 8, 9, 10),
                        },
                        pollEvents -> pollEvents.size() == 1 && pollEvents.getFirst().eventType() == PollEventType.FALLING
                )
        ).map(expectation -> DynamicTest.dynamicTest(expectation.name, () -> {
                var assertion = new AtomicBoolean(false);
                var fileOperations = new LineEventsFileOperations(expectation.events);

                PollEventsCallback callback = (poller, pollEvents) -> {
                    poller.stop();
                    pollEvents.forEach(System.out::println);

                    assertion.set(expectation.predicate.apply(pollEvents));
                };

                var poller = new EventPollerImpl(
                        1,
                        callback,
                        Duration.ZERO,
                        pollingOperations,
                        fileOperations
                );

                poller.run();

                assertThat(assertion.get()).isTrue();
            })
        );
    }


    class LineEventsFileOperations extends FileOperations.AbstractFileOperations {

        private final LineEvent.Serializer serializer = new LineEvent.Serializer(segmentAllocator);
        private final LineEvent[] events;

        LineEventsFileOperations(LineEvent[] events) {
            this.events = events;
        }

        @Override
        public <T> T read(int fd, int offset, int length, @NonNull BiFunction<MemorySegment, Integer, T> handler) {
            var segment = segmentAllocator.allocate(length);

            for (int i = 0; i < events.length; i++) {
                var slice = segment.asSlice(i * LineEvent.LAYOUT.byteSize(), LineEvent.LAYOUT.byteSize());
                var eventSegment = serializer.serialize(events[i]);
                slice.copyFrom(eventSegment);
            }

            return handler.apply(segment, length);
        }
    }
}