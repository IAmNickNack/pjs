package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineValues;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A GPIO port that uses native IOCTL calls to read and write pin values
 */
class NativePort implements GpioPort, AutoCloseable {

    private static final LineValues LOW_MASK = new LineValues(0, Long.MAX_VALUE);

    private final Logger logger = LoggerFactory.getLogger(NativePort.class);

    private final GpioPortConfig config;
    private final IoctlOperations ioctlOperations;
    private final FileDescriptor fileDescriptor;
    private final Set<GpioEventListener<GpioPort>> listeners = new CopyOnWriteArraySet<>();
    private final EventPoller eventPoller;
    private final PollEventsCallback pollEventsCallback;

//    private final Predicate<PollEvent> pollEventPredicate;

    /**
     * Create a new instance with event polling support
     * Create a new instance with no event polling support
     * @param config the port configuration
     * @param fileDescriptor the file descriptor for the GPIO port
     * @param ioctlOperations ioctl implementation
     */
    public NativePort(
            GpioPortConfig config,
            FileDescriptor fileDescriptor,
            IoctlOperations ioctlOperations,
            EventPoller.Factory eventPollerFactory

    ) {
        this.config = config;
        this.fileDescriptor = fileDescriptor;
        this.ioctlOperations = ioctlOperations;
        this.pollEventsCallback = new StabilityDebounceCallback(this::handleEventCallback, config.debounceDelay());
        this.eventPoller = eventPollerFactory.create(fileDescriptor, this.pollEventsCallback);
//        this.eventPoller = eventPollerFactory.create(fileDescriptor, this::eventPollerCallback);
//        if (NativePortProvider.isSoftwareDebounceEnabled()) {
//            logger.info("Enabling software debounce for GPIO port with debounce delay: {}us", config.debounceDelay());
//            this.pollEventPredicate = new ThrottleDebounceFilter(config.debounceDelay() * 1000L); // convert to ns
//        } else {
//            this.pollEventPredicate = _ -> true;
//        }
    }

    /**
     * Create a new instance with no event polling support
     * @param config the port configuration
     * @param fileDescriptor the file descriptor for the GPIO port
     * @param ioctlOperations ioctl implementation
     */
    public NativePort(
            GpioPortConfig config,
            FileDescriptor fileDescriptor,
            IoctlOperations ioctlOperations
    ) {
        this(config, fileDescriptor, ioctlOperations, EventPoller.NOOP_FACTORY);
    }

    @Override
    public GpioPortConfig getConfig() {
        return config;
    }

    @Override
    public Integer read() {
        var value = ioctlOperations.ioctl(fileDescriptor, GpioConstants.GPIO_V2_LINE_GET_VALUES_IOCTL, LOW_MASK);
        return (int)value.bits();
    }

    @Override
    public void write(Integer value) {
        var lineValues = new LineValues(value, Long.MAX_VALUE);
        ioctlOperations.ioctl(fileDescriptor, GpioConstants.GPIO_V2_LINE_SET_VALUES_IOCTL, lineValues);
    }

    @Override
    public void close() {
        logger.debug("Closing GPIO port with file descriptor: {}", fileDescriptor.fd());
        fileDescriptor.close();
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        if (listeners.add(listener) && !eventPoller.isRunning()) {
            eventPoller.start();
        }
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        if (listeners.remove(listener) && listeners.isEmpty() && eventPoller.isRunning()) {
            eventPoller.stop();
        }
    }

    private void handleEventCallback(EventPoller poller, List<PollEvent> pollEvents) {
        pollEvents.stream()
//                .filter(pollEventPredicate)
                .map(pollEvent -> new GpioChangeEvent<>(NativePort.this, pollEvent.asLineChangeEventType()))
                .forEach(event -> listeners.forEach(listener -> listener.onEvent(event)));
    }

    /**
     * Leading-edge, rate-limiting callback
     * <p>
     * Accept the first change and then ignore further changes for the debounce window
     * (or accept at most one event per window).
     * Useful when an immediate reaction is wanted but limit how often it can re-fire.
     */
    static class ThrottledDebounceCallback implements PollEventsCallback, AutoCloseable {
        private final PollEventsCallback delegate;
        private final DebounceFilter eventFilter;

        public ThrottledDebounceCallback(PollEventsCallback delegate, long debounce) {
            this.delegate = delegate;
            this.eventFilter = new DebounceFilter(debounce);
        }

        @Override
        public void callback(EventPoller poller, List<PollEvent> pollEvents) {
            var filteredEvents = pollEvents.stream().filter(eventFilter).toList();
            if (!filteredEvents.isEmpty()) {
                delegate.callback(poller, filteredEvents);
            }
        }

        @Override
        public void close() {
            // do nothing
        }
    }

    /**
     * Trailing-edge callback which propagates an event after a specified debounce period of stability.
     * <p>
     * Waits until the signal/value is unchanged for the debounce window, then emits the change.
     */
    static class StabilityDebounceCallback implements PollEventsCallback, AutoCloseable {

        private final PollEventsCallback delegate;
        private final long debounce;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final Object lock = new Object();

        private volatile PollEvent lastEvent = null;
        private ScheduledFuture<?> future;
        private final DebounceFilter eventFilter;

        /**
         * Constructor
         * @param delegate the delegate callback
         * @param debounce debounce period in microseconds
         */
        public StabilityDebounceCallback(PollEventsCallback delegate, long debounce) {
            this.delegate = delegate;
            this.debounce = debounce;
            this.eventFilter = new DebounceFilter(debounce);
        }

        @Override
        public void callback(EventPoller poller, List<PollEvent> pollEvents) {
            synchronized (lock) {
                var filteredEvents = pollEvents.stream().filter(eventFilter).toList();
                if (filteredEvents.isEmpty()) {
                    return;
                }

                lastEvent = filteredEvents.getLast();
                if (future != null) {
                    future.cancel(false);
                }
                future = scheduler.schedule(() -> {
                    synchronized (lock) {
                        future = null;
                        if ((lastEvent.timestamp() / 1000) >= debounce) {
                            delegate.callback(poller, filteredEvents);
                        }
                    }
                }, debounce, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public void close() {
            scheduler.shutdownNow();
        }
    }

    /**
     * Stateful predicate to filter events based on a provided debounce period
     */
    static class DebounceFilter implements Predicate<PollEvent> {
        private long last = 0;
        private final long debounce;

        public DebounceFilter(long debounce) {
            this.debounce = debounce;
        }

        @Override
        public boolean test(PollEvent pollEvent) {
            if ((pollEvent.timestamp() - last > debounce) || (last == 0)) {
                last = pollEvent.timestamp();
                return true;
            }
            return false;
        }
    }
}
