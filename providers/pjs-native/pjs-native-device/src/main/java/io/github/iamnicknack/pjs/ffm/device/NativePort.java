package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineValues;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
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

    public NativePort(GpioPortConfig config, FileDescriptor fileDescriptor, NativeContext nativeContext) {

        logger.debug("Creating GPIO port with file descriptor: {}", fileDescriptor.fd());

        this.config = config;
        this.fileDescriptor = fileDescriptor;
        this.ioctlOperations = new IoctlOperationsImpl(nativeContext);

        boolean softwareDebounce = NativePortProvider.isSoftwareDebounceEnabled();
        logger.debug("GPIO debounce software filter is {}", softwareDebounce ? "enabled" : "disabled");

        if (config.eventMode() != GpioEventMode.NONE) {
            this.eventPoller = new EventPoller(
                    fileDescriptor.fd(),
                    new DebouncedEventsCallback((softwareDebounce) ? config.debounceDelay() * 1000L : 0),
                    Duration.ofMillis(100),
                    nativeContext
            );
            this.eventPollingExecutorService = Executors.newSingleThreadExecutor(r -> {
                Objects.requireNonNull(r);
                var thread = new Thread(r, "gpio-event-poller-" + config.id());
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) ->
                        logger.error("Error on thread: {}", t.getName(), e)
                );
                return thread;
            });
        } else {
            this.eventPoller = null;
            this.eventPollingExecutorService = null;
        }
    }

    @Override
    public GpioPortConfig getConfig() {
        return config;
    }

    @Override
    public Integer read() {
        var value = ioctlOperations.ioctl(fileDescriptor.fd(), GpioConstants.GPIO_V2_LINE_GET_VALUES_IOCTL, LOW_MASK);
        return (int)value.bits();
    }

    @Override
    public void write(Integer value) {
        var lineValues = new LineValues(value, Long.MAX_VALUE);
        ioctlOperations.ioctl(fileDescriptor.fd(), GpioConstants.GPIO_V2_LINE_SET_VALUES_IOCTL, lineValues);
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
        if (eventPoller != null) {
            listeners.add(listener);
            if (!eventPoller.isRunning()) {
                eventPollingExecutorService.submit(eventPoller);
            }
        }
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        if (eventPoller != null) {
            listeners.remove(listener);
            if (listeners.isEmpty() && eventPoller.isRunning()) {
                eventPoller.stop();
            }
        }
    }

    /**
     * Callback operation for event polling which implements a software debounce filter
     */
    class DebouncedEventsCallback implements PollEventsCallback {

        private final Predicate<PollEvent> debouncer;

        /**
         * Constructor
         * @param debounceDelay the debounce delay in the whatever timeunit is used by the native context.
         *                      Any divisor or multiplier needs to be applied before passing it to this constructor.
         */
        public DebouncedEventsCallback(long debounceDelay) {
            this.debouncer = (debounceDelay > 0)
                    ? new DebounceFilter(debounceDelay)
                    : _ -> true;
        }

        @Override
        public void callback(EventPoller poller, List<PollEvent> pollEvents) {
            pollEvents.stream()
                    .filter(debouncer)
                    .map(pollEvent -> new GpioChangeEvent<>(NativePort.this, pollEvent.asLineChangeEventType()))
                    .forEach(gpioChangeEvent -> listeners
                            .forEach(gpioEventListener -> gpioEventListener.onEvent(gpioChangeEvent))
                    );
        }
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
