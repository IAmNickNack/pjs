package io.github.iamnicknack.pjs.ffm.event.debounce;

import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Trailing-edge callback which propagates an event after a specified debounce period of stability.
 * <p>
 * Waits until the signal/value is unchanged for the debounce window, then emits the change.
 */
public class StabilityDebounceCallback implements PollEventsCallback, AutoCloseable {

    private final PollEventsCallback delegate;
    private final long debounce;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();

    private volatile PollEvent lastEvent = null;
    private ScheduledFuture<?> future;
    private final DebounceFilter eventFilter;

    /**
     * Constructor
     *
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
            }, debounce, TimeUnit.MICROSECONDS);
        }
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
