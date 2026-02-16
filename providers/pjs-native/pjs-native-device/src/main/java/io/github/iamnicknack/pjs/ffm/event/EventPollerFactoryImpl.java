package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineEvent;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class EventPollerFactoryImpl implements EventPoller.Factory, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Duration timeout;
    private final PollingOperations pollingOperations;
    private final FileOperations fileOperations;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Objects.requireNonNull(r);
        var thread = new Thread(r, "gpio-event-poller");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) ->
                logger.error("Error on thread: {}", t.getName(), e)
        );
        return thread;
    });


    private static final MemorySegmentDeserializer<LineEvent> lineEventDeserializer = new LineEvent.Deserializer();
    private static final long LINE_EVENT_SIZE = LineEvent.LAYOUT.byteSize();

    public EventPollerFactoryImpl(
            Duration timeout,
            PollingOperations pollingOperations,
            FileOperations fileOperations
    ) {
        this.timeout = timeout;
        this.pollingOperations = pollingOperations;
        this.fileOperations = fileOperations;
    }

    @Override
    public EventPoller create(FileDescriptor fileDescriptor, PollEventsCallback pollEventsCallback) {
        return new Poller(fileDescriptor, pollEventsCallback);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private class Poller implements EventPoller {

        private volatile boolean running;
        private final FileDescriptor fileDescriptor;
        private final PollEventsCallback callback;

        private Poller(FileDescriptor fileDescriptor, PollEventsCallback callback) {
            this.fileDescriptor = fileDescriptor;
            this.callback = callback;
        }

        @Override
        public void start() {
            if (!running) {
                executorService.submit(this);
            }
        }

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            var pollData = new Poll(fileDescriptor.fd());
            logger.info("Starting event poller on fd {}", fileDescriptor.fd());
            running = true;

            try {
                while (running) {
                    var poll = pollingOperations.poll(pollData, (int) timeout.toMillis());
                    // got a change event?
                    if ((poll.revents() & Poll.Flags.POLLIN.value) != 0) {
                        var events = fileOperations.read(
                                fileDescriptor,
                                0,
                                (int) (16 * LINE_EVENT_SIZE),
                                (memorySegment, ignored) -> IntStream.range(0, 16)
                                        .mapToObj(index -> memorySegment.asSlice(index * LINE_EVENT_SIZE, LINE_EVENT_SIZE))
                                        .map(lineEventDeserializer::deserialize)
                                        .takeWhile(event -> event.timestampNs() != 0)
                                        .map(event -> new PollEvent(PollEventType.from(event.id()), event.timestampNs()))
                                        .toList()
                        );

                        // got some events
                        if (!events.isEmpty()) {
                            callback.callback(this, events);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error while polling for events on fd {}", fileDescriptor.fd(), e);
            }
        }
    }
}
