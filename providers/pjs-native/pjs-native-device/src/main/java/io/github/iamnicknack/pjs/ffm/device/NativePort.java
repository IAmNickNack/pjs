package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineValues;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.EventPollerImpl;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ExecutorService eventPollingExecutorService;

    private final Predicate<PollEvent> pollEventPredicate;

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
            @Nullable FileOperations fileOperations,
            @Nullable PollingOperations pollingOperations

    ) {
        this.config = config;
        this.fileDescriptor = fileDescriptor;
        this.ioctlOperations = ioctlOperations;
        if (pollingOperations != null) {
            this.eventPoller = new EventPollerImpl(
                    fileDescriptor.fd(),
                    this::eventPollerCallback,
                    Duration.ofMillis(100),
                    pollingOperations,
                    fileOperations
            );;
            this.eventPollingExecutorService = Executors.newSingleThreadExecutor(r -> {
                Objects.requireNonNull(r);
                var thread = new Thread(r, "gpio-event-poller-" + config.id());
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) ->
                        logger.error("Error on thread: {}", t.getName(), e)
                );
                return thread;
            });;
        } else {
            this.eventPoller = null;
            this.eventPollingExecutorService = null;
        }
        this.pollEventPredicate = (NativePortProvider.isSoftwareDebounceEnabled())
                ? new DebounceFilter(config.debounceDelay() * 1000L)
                : _ -> true;
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
        this(config, fileDescriptor, ioctlOperations, null, null);
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
        if (eventPollingExecutorService != null) {
            eventPollingExecutorService.shutdownNow();
        }
        logger.debug("Closing GPIO port with file descriptor: {}", fileDescriptor.fd());
        fileDescriptor.close();
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        if ((eventPoller != null) && (listeners.add(listener))) {
            if (!eventPoller.isRunning()) {
                eventPollingExecutorService.submit(eventPoller);
            }
        }
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        if ((eventPoller != null) && listeners.remove(listener)) {
            if (listeners.isEmpty() && eventPoller.isRunning()) {
                eventPoller.stop();
            }
        }
    }

    private void eventPollerCallback(EventPoller poller, List<PollEvent> pollEvents) {
        pollEvents.stream()
                .filter(pollEventPredicate)
                .map(pollEvent -> new GpioChangeEvent<>(NativePort.this, pollEvent.asLineChangeEventType()))
                .forEach(event -> listeners.forEach(listener -> listener.onEvent(event)));
    }

    /**
     * Stateful predicate, which can be used as a software debouncer
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
