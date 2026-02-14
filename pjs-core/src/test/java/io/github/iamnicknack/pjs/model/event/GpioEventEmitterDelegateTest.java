package io.github.iamnicknack.pjs.model.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GpioEventEmitterDelegateTest {

    static final class DummyEmitter implements GpioEventEmitter<DummyEmitter> {
        @Override
        public void addListener(GpioEventListener<DummyEmitter> listener) {
            // no-op for tests
        }

        @Override
        public void removeListener(GpioEventListener<DummyEmitter> listener) {
            // no-op for tests
        }
    }

    @Test
    void forwardsEventToRegisteredListeners() throws Exception {
        try (var delegate = new GpioEventEmitterDelegate<DummyEmitter>()) {
            var counter = new AtomicInteger();
            var received = new AtomicInteger();

            delegate.addListener(event -> {
                counter.incrementAndGet();
                received.set(event.eventType().value);
            });

            var event = new GpioChangeEvent<>(new DummyEmitter(), GpioChangeEventType.RISING);
            delegate.onEvent(event);

            assertEquals(1, counter.get(), "listener should be invoked exactly once");
            assertEquals(GpioChangeEventType.RISING.value, received.get(), "listener should receive the event");
        }
    }

    @Test
    void exceptionInListenerDoesNotPreventOtherListeners() throws Exception {
        try (var delegate = new GpioEventEmitterDelegate<DummyEmitter>()) {
            var counter = new AtomicInteger();

            delegate.addListener(event -> { throw new RuntimeException("boom"); });
            delegate.addListener(event -> counter.incrementAndGet());

            var event = new GpioChangeEvent<>(new DummyEmitter(), GpioChangeEventType.FALLING);
            delegate.onEvent(event);

            // second listener should still run
            assertEquals(1, counter.get());
        }
    }

    @Test
    void addAndRemoveListenersAffectCount() throws Exception {
        try (var delegate = new GpioEventEmitterDelegate<DummyEmitter>()) {
            var l1 = (GpioEventListener<DummyEmitter>) event -> {};
            var l2 = (GpioEventListener<DummyEmitter>) event -> {};

            assertEquals(0, delegate.getListenerCount());
            delegate.addListener(l1);
            assertEquals(1, delegate.getListenerCount());
            delegate.addListener(l2);
            assertEquals(2, delegate.getListenerCount());

            delegate.removeListener(l1);
            assertEquals(1, delegate.getListenerCount());
            delegate.removeListener(l2);
            assertEquals(0, delegate.getListenerCount());
        }
    }

    @Test
    void closeShutsDownExecutorAndRejectsSubmissions() throws Exception {
        var delegate = new GpioEventEmitterDelegate<DummyEmitter>();
        try {
            delegate.addListener(event -> {});
            // close the delegate which shuts down the executor
            delegate.close();

            var event = new GpioChangeEvent<>(new DummyEmitter(), GpioChangeEventType.RISING);
            assertThrows(RejectedExecutionException.class, () -> delegate.onEvent(event));
        } finally {
            // best-effort close if not already
            try { delegate.close(); } catch (Exception ignored) {}
        }
    }
}

