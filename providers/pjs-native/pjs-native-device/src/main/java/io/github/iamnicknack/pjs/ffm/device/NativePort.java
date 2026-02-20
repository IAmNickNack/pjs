package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineValues;
import io.github.iamnicknack.pjs.ffm.event.DebounceStrategy;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;
import io.github.iamnicknack.pjs.ffm.event.debounce.TrailingEdgeDebounceCallback;
import io.github.iamnicknack.pjs.ffm.event.debounce.LeadingEdgeDebounceCallback;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
    private final AutoCloseable closeableCallback;


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
        PollEventsCallback pollEventsCallback = switch (DebounceStrategy.fromProperty()) {
            case SOFTWARE_LEADING_EDGE -> new LeadingEdgeDebounceCallback(this::handleEventCallback, config.debounceDelay() * 1000L);
            case SOFTWARE_TRAILING_EDGE -> new TrailingEdgeDebounceCallback(this::handleEventCallback, config.debounceDelay() * 1000L);
            case HARDWARE -> this::handleEventCallback;
        };
        if (pollEventsCallback instanceof AutoCloseable closeable) {
            this.closeableCallback = closeable;
        } else {
            this.closeableCallback = null;
        }
        this.eventPoller = eventPollerFactory.create(fileDescriptor, pollEventsCallback);
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
        if (closeableCallback != null) {
            try {
                closeableCallback.close();
            } catch (Exception e) {
                logger.warn("Failed to close event poller", e);
            }
        }
        eventPoller.stop();
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
                .map(pollEvent -> new GpioChangeEvent<>(NativePort.this, pollEvent.asLineChangeEventType()))
                .forEach(event -> listeners.forEach(listener -> listener.onEvent(event)));
    }
}
