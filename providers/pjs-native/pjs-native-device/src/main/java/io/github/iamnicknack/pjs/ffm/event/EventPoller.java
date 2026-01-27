package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineEvent;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.stream.IntStream;

public class EventPoller implements Runnable {

    private static final long LINE_EVENT_SIZE = LineEvent.LAYOUT.byteSize();

    private final Logger logger = LoggerFactory.getLogger(EventPoller.class);

    private final int fd;
    private final PollEventsCallback callback;
    private final Duration timeout;

    private final MemorySegmentDeserializer<LineEvent> deserializer = new LineEvent.Deserializer();
    private final PollingOperations pollingOperations;
    private final FileOperations fileOperations;

    private volatile boolean running = false;

    /**
     * Event poller with default polling operations
     * @param fd the file descriptor to watch
     * @param callback function invoked when change is detected
     * @param timeout timeout for poll operation
     * @param nativeContext the context to use for native operations
     */
    public EventPoller(
            int fd,
            PollEventsCallback callback,
            Duration timeout,
            NativeContext nativeContext
    ) {
        this(fd, callback, timeout, new PollingOperationsImpl(nativeContext), new FileOperationsImpl(nativeContext));
    }

    /**
     * Event poller with default polling operations
     * @param fd the file descriptor to watch
     * @param callback function invoked when change is detected
     * @param pollingOperations specific polling operations (can be faked for testing)
     * @param fileOperations specific file operations (can be faked for testing)
     */
    public EventPoller(
            int fd,
            PollEventsCallback callback,
            Duration timeout,
            PollingOperations pollingOperations,
            FileOperations fileOperations
    ) {
        this.fd = fd;
        this.callback = callback;
        this.timeout = timeout;
        this.pollingOperations = pollingOperations;
        this.fileOperations = fileOperations;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        var pollData = new Poll(this.fd);
        logger.info("Starting event poller on fd {}", fd);
        running = true;

        try {
            while (running) {
                var poll = pollingOperations.poll(pollData, (int) timeout.toMillis());
                // got a change event?
                if ((poll.revents() & Poll.Flags.POLLIN.value) != 0) {
                    var events = fileOperations.read(
                            fd,
                            0,
                            (int) (16 * LINE_EVENT_SIZE),
                            (memorySegment, ignored) -> IntStream.range(0, 16)
                                    .mapToObj(index -> memorySegment.asSlice(index * LINE_EVENT_SIZE, LINE_EVENT_SIZE))
                                    .map(deserializer::deserialize)
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
            logger.error("Error while polling for events on fd {}", fd, e);
        }
    }
}
