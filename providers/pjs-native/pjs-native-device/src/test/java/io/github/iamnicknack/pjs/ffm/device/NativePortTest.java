package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractFileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractIoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineValues;
import io.github.iamnicknack.pjs.ffm.event.DebounceStrategy;
import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventType;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class NativePortTest {

    private final FileDescriptor fileDescriptor = new AbstractFileOperations() {}.createFileDescriptor(1);
    private final IoctlOperations ioctlOperations = AbstractIoctlOperations.builder()
            .addHandler(GpioConstants.GPIO_V2_LINE_GET_VALUES_IOCTL)
            .addHandler(GpioConstants.GPIO_V2_LINE_SET_VALUES_IOCTL)
            .build();

    @Test
    void beforeEach() {
        System.setProperty(DebounceStrategy.PROPERTY_KEY, DebounceStrategy.SOFTWARE_LEADING_EDGE.name());
    }

    @Test
    void debouncedEventsAreTriggeredAfterSettling() {
        var pollerFactory = new FakeEventPollerFactory();
        var config = GpioPortConfig.builder()
                .pin(1)
                .debounceDelay(100)
                .build();

        var eventCount = new AtomicInteger(0);

        var port = new NativePort(config, fileDescriptor, ioctlOperations, pollerFactory);
        port.addListener(_ -> eventCount.incrementAndGet());

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 1_000));
        assertThat(eventCount.get()).isEqualTo(1);

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 2_000));
        assertThat(eventCount.get()).isEqualTo(1);

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 101_000));
        assertThat(eventCount.get()).isEqualTo(1);

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 201_000));
        assertThat(eventCount.get()).isEqualTo(2);
    }

    @Test
    void softwareDebounceIsNotActive() {
        System.setProperty(DebounceStrategy.PROPERTY_KEY, DebounceStrategy.HARDWARE.name());
        var pollerFactory = new FakeEventPollerFactory();
        var config = GpioPortConfig.builder()
                .pin(1)
                .debounceDelay(100)
                .build();

        var eventCount = new AtomicInteger(0);

        var port = new NativePort(config, fileDescriptor, ioctlOperations, pollerFactory);
        port.addListener(_ -> eventCount.incrementAndGet());

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 1_000));
        assertThat(eventCount.get()).isEqualTo(1);

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 1_001));
        assertThat(eventCount.get()).isEqualTo(2);
    }

    @Test
    void canAddAndRemoveListeners() {
        var pollerFactory = new FakeEventPollerFactory();
        var config = GpioPortConfig.builder()
                .pin(1)
                .debounceDelay(100)
                .build();

        var eventCount = new AtomicInteger(0);

        var port = new NativePort(config, fileDescriptor, ioctlOperations, pollerFactory);

        GpioEventListener<GpioPort> listener1 = _ -> eventCount.incrementAndGet();
        GpioEventListener<GpioPort> listener2 = _ -> eventCount.incrementAndGet();

        port.addListener(listener1);
        port.addListener(listener2);

        pollerFactory.poke(new PollEvent(PollEventType.RISING, 1_000));
        assertThat(eventCount.get()).isEqualTo(2);

        port.removeListener(listener1);
        pollerFactory.poke(new PollEvent(PollEventType.RISING, 102_000));
        assertThat(eventCount.get()).isEqualTo(3);

        port.removeListener(listener2);
        pollerFactory.poke(new PollEvent(PollEventType.RISING, 203_000));
        assertThat(eventCount.get()).isEqualTo(3);
    }

    @Test
    void canReadWriteValues() {
        var lineValues = new AtomicReference<LineValues>();
        var ioctlOperations = AbstractIoctlOperations.builder()
                .addHandler(GpioConstants.GPIO_V2_LINE_GET_VALUES_IOCTL, (_, _, _) -> lineValues.get())
                .addHandler(GpioConstants.GPIO_V2_LINE_SET_VALUES_IOCTL, (_, _, values) -> {
                    lineValues.set((LineValues) values);
                    return values;
                })
                .build();
        var config = GpioPortConfig.builder()
                .pin(1)
                .build();

        var port = new NativePort(config, fileDescriptor, ioctlOperations);
        port.write(0x55);
        assertThat(port.read()).isEqualTo(0x55);
    }

    @Test
    void fileDescriptorIsClosed() {
        var isClosed = new AtomicBoolean(false);
        var fileOperations = new AbstractFileOperations() {
            @Override
            public int close(int fd) {
                isClosed.set(true);
                return 0;
            }
        };
        var fileDescriptor = fileOperations.createFileDescriptor(1);
        var config = GpioPortConfig.builder()
                .pin(1)
                .build();

        try (var _ = new NativePort(config, fileDescriptor, ioctlOperations)) {
            assertThat(isClosed.get()).isFalse();
        }
        assertThat(isClosed.get()).isTrue();
    }

    static class FakeEventPoller implements EventPoller {
        private boolean running = false;
        private final PollEventsCallback pollEventsCallback;

        public FakeEventPoller(PollEventsCallback pollEventsCallback) {
            this.pollEventsCallback = pollEventsCallback;
        }

        @Override
        public void start() {
            running = true;
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
            // do nothing
        }
    }

    static class FakeEventPollerFactory implements EventPoller.Factory {
        private FakeEventPoller poller;

        @Override
        public EventPoller create(FileDescriptor ignored, PollEventsCallback pollEventsCallback) {
            poller = new FakeEventPoller(pollEventsCallback);
            return poller;
        }

        public void poke(PollEvent... events) {
            if ((poller != null) && (poller.isRunning())) {
                poller.pollEventsCallback.callback(poller, Arrays.stream(events).toList());
            }
        }
    }
}