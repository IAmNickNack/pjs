package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineEvent;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * {@inheritDoc}
 */
public class EventPollerFactoryImpl implements EventPoller.Factory {

    private static final MemorySegmentDeserializer<LineEvent> lineEventDeserializer = new LineEvent.Deserializer();
    private static final int LINE_EVENT_SIZE = (int) LineEvent.LAYOUT.byteSize();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService;
    private final List<EventPoller> pollers = new ArrayList<>();

    private final Duration timeout;
    private final PollingOperations pollingOperations;
    private final FileOperations fileOperations;

    public EventPollerFactoryImpl(
            Duration timeout,
            PollingOperations pollingOperations,
            FileOperations fileOperations
    ) {
        this(timeout, pollingOperations, fileOperations, 4);
    }

    /**
     * Constructor.
     * @param timeout timeout for the native poll operation
     * @param pollingOperations polling operations
     * @param fileOperations file operations
     */
    public EventPollerFactoryImpl(
            Duration timeout,
            PollingOperations pollingOperations,
            FileOperations fileOperations,
            int threadPoolSize
    ) {
        this.timeout = timeout;
        this.pollingOperations = pollingOperations;
        this.fileOperations = fileOperations;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, Thread.ofPlatform()
                .name("gpio-event-poller-", 0)
                .uncaughtExceptionHandler((t, e) -> logger.error("Error on thread: {}", t.getName(), e))
                .daemon(true)
                .factory()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventPoller create(FileDescriptor fileDescriptor, PollEventsCallback pollEventsCallback) {
        logger.debug("Creating poller for fd: {}, with callback type: {}",
                fileDescriptor.fd(),
                pollEventsCallback.getClass().getSimpleName()
        );
        var poller = new Poller(
                new DefaultEventPollerOperation(fileDescriptor, timeout),
                new DefaultEventReaderOperation(fileDescriptor),
                pollEventsCallback
        );
        pollers.add(poller);
        return poller;
    }

    /**
     * Stops the executor service used for executing pollers.
     */
    @Override
    public void close() {
        pollers.forEach(EventPoller::stop);
        executorService.shutdown();
    }

    /**
     * Poller implementation.
     */
    private class Poller implements EventPoller {

        private volatile boolean running;
        private final EventPollerOperation eventPollerOperation;
        private final EventReaderOperation eventReaderOperation;
        private final PollEventsCallback callback;
        private final BlockingQueue<Boolean> stopSignal = new ArrayBlockingQueue<>(1);

        private Poller(
                EventPollerOperation eventPollerOperation,
                EventReaderOperation eventReaderOperation,
                PollEventsCallback callback) {
            this.eventPollerOperation = eventPollerOperation;
            this.eventReaderOperation = eventReaderOperation;
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
            if (!running) {
                return;
            }

            logger.info("Stopping event poller");
            running = false;
            try {
                stopSignal.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for poller to stop", e);
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            if (running) {
                logger.warn("Event poller is already running");
                return;
            }
            logger.info("Starting event poller");
            running = true;
            stopSignal.clear();

            try(var arena = Arena.ofConfined()) {
                var eventBuffer = arena.allocate(LINE_EVENT_SIZE * 16L);

                while (running) {
                    var poll = eventPollerOperation.poll();
                    // got a change event?
                    if ((poll.revents() & (Poll.Flags.POLLIN.value | Poll.Flags.POLLPRI.value)) != 0) {
                        if (!running) {
                            break;
                        }
                        var bytesRead = eventReaderOperation.read(eventBuffer);
                        var events = new ArrayList<PollEvent>();

                        for (int i = 0; i < bytesRead; i += LINE_EVENT_SIZE) {
                            var lineEventSegment = eventBuffer.asSlice(i, LINE_EVENT_SIZE);
                            var lineEvent = lineEventDeserializer.deserialize(lineEventSegment);

                            if (lineEvent.timestampNs() != 0) {
                                var pollEvent = new PollEvent(PollEventType.from(lineEvent.id()), lineEvent.timestampNs(), lineEvent.offset());
                                events.add(pollEvent);
                            }
                        }

                        // got some events
                        if (!events.isEmpty()) {
                            callback.callback(this, events);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error while polling for events", e);
            }

            stopSignal.add(true);
        }
    }


    @FunctionalInterface
    interface EventPollerOperation {
        Poll poll();
    }

    private class DefaultEventPollerOperation implements EventPollerOperation {
        private final FileDescriptor fileDescriptor;
        private final Duration timeout;

        public DefaultEventPollerOperation(FileDescriptor fileDescriptor, Duration timeout) {
            this.fileDescriptor = fileDescriptor;
            this.timeout = timeout;
        }

        @Override
        public Poll poll() {
            var poll = new Poll(fileDescriptor.fd());
            return pollingOperations.poll(poll, (int) timeout.toMillis());
        }
    }

    @FunctionalInterface
    interface EventReaderOperation {
        int read(MemorySegment buffer);
    }

    private class DefaultEventReaderOperation implements EventReaderOperation {
        private final FileDescriptor fileDescriptor;

        public DefaultEventReaderOperation(FileDescriptor fileDescriptor) {
            this.fileDescriptor = fileDescriptor;
        }

        @Override
        public int read(MemorySegment buffer) {
            return fileOperations.read(fileDescriptor.fd(), buffer, (int) buffer.byteSize());
        }
    }
}
