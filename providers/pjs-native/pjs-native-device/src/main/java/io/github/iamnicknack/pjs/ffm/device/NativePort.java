package io.github.iamnicknack.pjs.ffm.device;

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
        this.eventPoller = new EventPoller(fileDescriptor.fd(), this::pollEventsCallback, Duration.ofMillis(100), nativeContext);
        this.eventPollingExecutorService = Executors.newSingleThreadExecutor(r -> {
            Objects.requireNonNull(r);
            var thread = new Thread(r, "gpio-event-poller-" + config.id());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) ->
                    logger.error("Error on thread: {}", t.getName(), e)
            );
            return thread;
        });
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
        eventPollingExecutorService.shutdownNow();
        logger.debug("Closing GPIO port with file descriptor: {}", fileDescriptor.fd());
        fileDescriptor.close();
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
         listeners.add(listener);
         if (!eventPoller.isRunning()) {
            eventPollingExecutorService.submit(eventPoller);
        }
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() &&  eventPoller.isRunning()) {
            eventPoller.stop();
        }
    }

    private void pollEventsCallback(EventPoller eventPoller, List<PollEvent> pollEvents) {
        pollEvents.stream()
                .map(pollEvent -> new GpioChangeEvent<>(this, pollEvent.asLineChangeEventType()))
                .forEach(gpioChangeEvent -> listeners
                        .forEach(gpioEventListener -> gpioEventListener.onEvent(gpioChangeEvent))
                );
    }
}
