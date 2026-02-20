package io.github.iamnicknack.pjs.ffm.event.debounce;

import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;

import java.util.List;

/**
 * Leading-edge, rate-limiting callback
 * <p>
 * Accept the first change and then ignore further changes for the debounce window
 * (or accept at most one event per window).
 * Useful when an immediate reaction is wanted but limit how often it can re-fire.
 */
public class ThrottledDebounceCallback implements PollEventsCallback, AutoCloseable {
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
